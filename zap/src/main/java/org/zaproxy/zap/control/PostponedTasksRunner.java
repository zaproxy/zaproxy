/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2022 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.control;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/** Runner of postponed tasks, e.g. uninstallating an add-on. */
class PostponedTasksRunner {

    private static final Logger LOGGER = LogManager.getLogger(PostponedTasksRunner.class);

    private static final String TASKS_BASE_KEY = "postponedTasks";
    private static final String TASKS_KEY = TASKS_BASE_KEY + ".task";

    private final ZapXmlConfiguration config;
    private final AddOnCollection aoc;
    private final List<Task> tasks;

    PostponedTasksRunner(ZapXmlConfiguration config, AddOnCollection aoc) {
        this.config = config;
        this.aoc = aoc;
        this.tasks = readTasks(config);
    }

    List<Task> getTasks() {
        return tasks;
    }

    private static List<Task> readTasks(ZapXmlConfiguration config) {
        List<HierarchicalConfiguration> savedTasks = config.configurationsAt(TASKS_KEY);

        List<Task> tasks = new ArrayList<>(0);
        for (HierarchicalConfiguration savedTask : savedTasks) {
            Task task = createTask(savedTask);
            if (task != null) {
                tasks.add(task);
            }
        }

        return tasks;
    }

    public void run() {
        for (Iterator<Task> it = tasks.iterator(); it.hasNext(); ) {
            it.next().execute(aoc);
            it.remove();
        }

        saveTasks();
    }

    public void addUninstallAddOnTask(AddOn addOn) {
        tasks.add(new UninstallAddOnTask(addOn));

        saveTasks();
    }

    public void addDeleteFileTask(Path path) {
        tasks.add(new DeleteFileTask(path));

        saveTasks();
    }

    private void saveTasks() {
        config.clearTree(TASKS_BASE_KEY);

        try {
            int i = 0;
            for (Task task : tasks) {
                task.save(config, TASKS_KEY + "(" + i + ").");

                i++;
            }

            config.save();
        } catch (Exception e) {
            LOGGER.error("Failed to save the postponed tasks:", e);
        }
    }

    private static Task createTask(HierarchicalConfiguration data) {
        Task.Type type = readType(data);
        if (type == null) {
            return null;
        }

        switch (type) {
            case UNINSTALL_ADD_ON:
                return UninstallAddOnTask.create(data);

            case DELETE_FILE:
                return DeleteFileTask.create(data);

            default:
                LOGGER.error("Ignoring unsupported postponed task type: {}", type);
                return null;
        }
    }

    private static Task.Type readType(HierarchicalConfiguration savedData) {
        String typeName = savedData.getString(Task.TYPE_KEY, "");
        if (typeName.isBlank()) {
            return null;
        }

        try {
            return Task.Type.valueOf(typeName);
        } catch (Exception e) {
            LOGGER.warn("Failed to create postponed task type: {}", typeName);
        }
        return null;
    }

    abstract static class Task {

        private static final String TYPE_KEY = "type";

        enum Type {
            UNINSTALL_ADD_ON,
            DELETE_FILE
        }

        private final Type type;

        protected Task(Type type) {
            this.type = type;
        }

        Type getType() {
            return type;
        }

        abstract void execute(AddOnCollection aoc);

        final void save(ZapXmlConfiguration config, String keyPrefix) {
            config.setProperty(keyPrefix + TYPE_KEY, type.name());

            saveData(config, keyPrefix);
        }

        protected abstract void saveData(ZapXmlConfiguration config, String keyPrefix);
    }

    static class UninstallAddOnTask extends Task {

        private static final String ADD_ON_KEY = "addOn";

        private final AddOn addOn;

        private UninstallAddOnTask(AddOn addOn) {
            super(Task.Type.UNINSTALL_ADD_ON);

            this.addOn = addOn;
        }

        AddOn getAddOn() {
            return addOn;
        }

        @Override
        void execute(AddOnCollection aoc) {
            LOGGER.info("Executing postponed task, uninstalling add-on: {}", addOn);

            AddOnInstaller.uninstallAddOnFiles(
                    addOn,
                    NullUninstallationProgressCallBack.getSingleton(),
                    Collections.emptySet(),
                    null);
            AddOnInstaller.uninstallAddOnLibs(addOn);

            AddOn presentAddOn = aoc.getAddOn(addOn.getId());
            if (presentAddOn != null && addOn.getFile().equals(presentAddOn.getFile())) {
                aoc.removeAddOn(presentAddOn);
            }

            Path path = addOn.getFile().toPath();
            try {
                Files.delete(path);
            } catch (IOException e) {
                LOGGER.warn("Failed to delete add-on file: {}", path, e);
            }
        }

        @Override
        protected void saveData(ZapXmlConfiguration config, String keyPrefix) {
            config.setProperty(
                    keyPrefix + ADD_ON_KEY,
                    addOn.getFile().toPath().toAbsolutePath().normalize().toString());
        }

        static UninstallAddOnTask create(HierarchicalConfiguration data) {
            String path = data.getString(ADD_ON_KEY, "");
            if (path.isBlank()) {
                return null;
            }

            Path file = Paths.get(path);
            if (Files.notExists(file)) {
                LOGGER.warn("Ignoring postponed task, add-on file no longer exists: {}", path);
                return null;
            }

            AddOn addOn;
            try {
                addOn = new AddOn(file);
            } catch (IOException e) {
                LOGGER.warn("Ignoring postponed task, add-on file is not valid: {}", path, e);
                return null;
            }
            return new UninstallAddOnTask(addOn);
        }
    }

    static class DeleteFileTask extends Task {

        private static final String FILE_KEY = "file";

        private final Path file;

        private DeleteFileTask(Path file) {
            super(Task.Type.DELETE_FILE);

            this.file = file;
        }

        Path getFile() {
            return file;
        }

        @Override
        void execute(AddOnCollection aoc) {
            LOGGER.info("Executing postponed task, deleting bundled add-on file: {}", file);

            try {
                Files.delete(file);
            } catch (IOException e) {
                LOGGER.warn("Failed to delete the file: {}", file, e);
            }
        }

        @Override
        protected void saveData(ZapXmlConfiguration config, String keyPrefix) {
            config.setProperty(keyPrefix + FILE_KEY, file.toAbsolutePath().normalize().toString());
        }

        static DeleteFileTask create(HierarchicalConfiguration data) {
            String path = data.getString(FILE_KEY, "");
            if (path == null || path.isBlank()) {
                return null;
            }

            Path file = Paths.get(path);
            if (Files.notExists(file)) {
                LOGGER.warn(
                        "Ignoring postponed task, add-on bundled file no longer exists: {}", path);
                return null;
            }

            Path homeDir = Paths.get(Constant.getZapHome());
            if (!file.startsWith(homeDir)) {
                LOGGER.warn(
                        "Ignoring postponed task, add-on bundled file is not under the home directory: {}",
                        path);
                return null;
            }

            return new DeleteFileTask(file);
        }
    }
}
