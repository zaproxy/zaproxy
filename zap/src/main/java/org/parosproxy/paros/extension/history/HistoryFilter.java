/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2010 The ZAP Development Team
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
package org.parosproxy.paros.extension.history;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.model.HistoryReference;
import org.zaproxy.zap.utils.Enableable;

public class HistoryFilter extends Enableable {

    public static final String NOTES_IGNORE =
            Constant.messages.getString("history.filter.notes.ignore");
    public static final String NOTES_PRESENT =
            Constant.messages.getString("history.filter.notes.present");
    public static final String NOTES_ABSENT =
            Constant.messages.getString("history.filter.notes.absent");
    public static final String[] NOTES_OPTIONS = {NOTES_IGNORE, NOTES_PRESENT, NOTES_ABSENT};

    private String name;
    private List<String> methodList = new ArrayList<>();
    private List<Integer> codeList = new ArrayList<>();
    private List<String> riskList = new ArrayList<>();
    private List<String> confidenceList = new ArrayList<>();
    private List<String> tagList = new ArrayList<>();
    private String note = null;
    private List<Pattern> urlIncPatternList = new ArrayList<>();
    private List<Pattern> urlExcPatternList = new ArrayList<>();
    private Optional<Long> startTimeSentInMs = Optional.empty();

    private Logger logger = LogManager.getLogger(HistoryFilter.class);

    public void setMethods(List<String> methods) {
        methodList.clear();
        methodList.addAll(methods);
    }

    public void setCodes(List<Integer> codes) {
        codeList.clear();
        codeList.addAll(codes);
    }

    public void setTags(List<String> tags) {
        tagList.clear();
        tagList.addAll(tags);
    }

    public void setRisks(List<String> risks) {
        riskList.clear();
        riskList.addAll(risks);
    }

    public void setReliabilities(List<String> reliabilities) {
        confidenceList.clear();
        confidenceList.addAll(reliabilities);
    }

    public Optional<Long> getStartTimeSentInMs() {
        return startTimeSentInMs;
    }

    public void setStartTimeSentInMs(Optional<Long> startTimeSentInMs) {
        this.startTimeSentInMs = startTimeSentInMs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void reset() {
        this.methodList.clear();
        this.codeList.clear();
        this.tagList.clear();
        this.riskList.clear();
        this.confidenceList.clear();
        this.note = null;
        this.startTimeSentInMs = Optional.empty();
    }

    public boolean matches(HistoryReference historyRef) {
        try {
            if (methodList.size() > 0 && !methodList.contains(historyRef.getMethod())) {
                return false;
            }
            if (codeList.size() > 0 && !codeList.contains(historyRef.getStatusCode())) {
                return false;
            }
            boolean foundTag = false;
            if (tagList.size() > 0) {
                for (String tag : historyRef.getTags()) {
                    if (tagList.contains(tag)) {
                        foundTag = true;
                        break;
                    }
                }
                if (!foundTag) {
                    return false;
                }
            }
            boolean foundAlert = false;
            if (riskList.size() > 0 || confidenceList.size() > 0) {
                for (Alert alert : historyRef.getAlerts()) {
                    if ((riskList.isEmpty() || riskList.contains(Alert.MSG_RISK[alert.getRisk()]))
                            && (confidenceList.isEmpty()
                                    || confidenceList.contains(
                                            Alert.MSG_CONFIDENCE[alert.getConfidence()]))) {
                        foundAlert = true;
                        break;
                    }
                }
                if (!foundAlert) {
                    return false;
                }
            }
            if (note != null && !note.equals(NOTES_IGNORE)) {
                String noteStr = historyRef.getHttpMessage().getNote();
                boolean notePresent = noteStr != null && noteStr.length() > 0;
                if (note.equals(NOTES_PRESENT) != notePresent) {
                    return false;
                }
            }
            String url = historyRef.getURI().toString();
            if (this.urlExcPatternList != null && this.urlExcPatternList.size() > 0) {
                for (Pattern p : this.urlExcPatternList) {
                    if (p.matcher(url).matches()) {
                        return false;
                    }
                }
            }
            if (this.urlIncPatternList != null && this.urlIncPatternList.size() > 0) {
                // URL include patterns work slightly differently
                // If any are supplied then one must match for the record to be included
                boolean matched = false;
                for (Pattern p : this.urlIncPatternList) {
                    if (p.matcher(url).matches()) {
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    return false;
                }
            }
            if (startTimeSentInMs.isPresent()
                    && historyRef.getTimeSentMillis() < startTimeSentInMs.get()) {
                return false;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return true;
    }

    public String toShortString() {
        StringBuilder sb = new StringBuilder(250);
        if (methodList.size() > 0) {
            sb.append(Constant.messages.getString("history.filter.desc.label.methods"));
        }
        if (codeList.size() > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(Constant.messages.getString("history.filter.desc.label.codes"));
        }
        if (tagList.size() > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(Constant.messages.getString("history.filter.desc.label.tags"));
        }
        if (riskList.size() > 0 || confidenceList.size() > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(Constant.messages.getString("history.filter.desc.label.alerts"));
        }
        if (note != null && !note.equals(NOTES_IGNORE)) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(Constant.messages.getString("history.filter.desc.label.notes"));
        }
        if (urlIncPatternList != null && urlIncPatternList.size() > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(Constant.messages.getString("history.filter.desc.label.urlincregex"));
        }
        if (urlExcPatternList != null && urlExcPatternList.size() > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(Constant.messages.getString("history.filter.desc.label.urlexcregex"));
        }
        if (startTimeSentInMs.isPresent()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(Constant.messages.getString("history.filter.desc.label.startTimeSentInMs"));
        }

        if (sb.length() > 0) {
            sb.insert(0, ' ');
            sb.insert(0, Constant.messages.getString("history.filter.label.on"));
            sb.insert(0, ' ');
            sb.insert(0, Constant.messages.getString("history.filter.label.filter"));
        } else {
            sb.append(Constant.messages.getString("history.filter.label.filter"));
            sb.append(' ');
            sb.append(Constant.messages.getString("history.filter.label.off"));
        }
        return sb.toString();
    }

    public String toLongString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Constant.messages.getString("history.filter.label.filter"));
        sb.append(' ');
        boolean empty = true;
        if (methodList.size() > 0) {
            empty = false;
            sb.append(Constant.messages.getString("history.filter.label.methods"));
            sb.append(' ');
            for (String method : methodList) {
                sb.append(method);
                sb.append(' ');
            }
        }
        if (codeList.size() > 0) {
            empty = false;
            sb.append(Constant.messages.getString("history.filter.label.codes"));
            sb.append(' ');
            Integer lastCode = null;
            boolean inBlock = false;
            for (Integer code : codeList) {
                if (lastCode == null) {
                    // very first one
                    sb.append(code);
                } else if (code == lastCode + 1) {
                    // next in sequence
                    inBlock = true;
                } else if (inBlock) {
                    // no longer in a consecutive set of codes
                    sb.append('-');
                    sb.append(lastCode);
                    sb.append(' ');
                    sb.append(code);
                    inBlock = false;
                } else {
                    // Not in a block of codes
                    sb.append(' ');
                    sb.append(code);
                }
                lastCode = code;
            }
            if (inBlock) {
                // finish off the series
                sb.append('-');
                sb.append(lastCode);
            }
            sb.append(' ');
        }
        if (tagList.size() > 0) {
            empty = false;
            sb.append(Constant.messages.getString("history.filter.label.tags"));
            sb.append(' ');
            for (String tag : tagList) {
                sb.append(tag);
                sb.append(' ');
            }
        }
        if (riskList.size() > 0 || confidenceList.size() > 0) {
            empty = false;
            sb.append(Constant.messages.getString("history.filter.label.alerts"));
            sb.append(' ');
            for (String risk : riskList) {
                sb.append(risk);
                sb.append(' ');
            }
            for (String rel : confidenceList) {
                sb.append(rel);
                sb.append(' ');
            }
        }
        if (note != null && !note.equals(NOTES_IGNORE)) {
            empty = false;
            sb.append(Constant.messages.getString("history.filter.label.notes"));
            sb.append(' ');
            sb.append(note);
        }
        if (urlIncPatternList != null && urlIncPatternList.size() > 0) {
            empty = false;
            sb.append(Constant.messages.getString("history.filter.label.urlincregex"));
            sb.append(' ');
            for (Pattern excPattern : urlIncPatternList) {
                sb.append(excPattern.pattern());
                sb.append(' ');
            }
        }
        if (urlExcPatternList != null && urlExcPatternList.size() > 0) {
            empty = false;
            sb.append(Constant.messages.getString("history.filter.label.urlexcregex"));
            sb.append(' ');
            for (Pattern excPattern : urlExcPatternList) {
                sb.append(excPattern.pattern());
                sb.append(' ');
            }
        }
        if (startTimeSentInMs.isPresent()) {
            empty = false;
            sb.append(Constant.messages.getString("history.filter.label.startTimeSentInMs"));
            sb.append(' ');
            DateFormat dateFormat =
                    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
            sb.append(dateFormat.format(new Date(startTimeSentInMs.get())));
        }
        if (empty) {
            sb.append(Constant.messages.getString("history.filter.label.off"));
        }
        return sb.toString();
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<Pattern> getUrlIncPatternList() {
        return urlIncPatternList;
    }

    public String[] getUrlIncPatternValueList() {
        return patternToStringArray(urlIncPatternList);
    }

    public void setUrlIncPatternList(List<Pattern> urlIncPatternList) {
        this.urlIncPatternList = urlIncPatternList;
    }

    public void setUrlIncPatternList(String[] urlIncPatternValueList) {
        setUrlIncPatternList(stringArrayToPatternList(urlIncPatternValueList));
    }

    public List<Pattern> getUrlExcPatternList() {
        return urlExcPatternList;
    }

    public String[] getUrlExcPatternValueList() {
        return patternToStringArray(urlExcPatternList);
    }

    public void setUrlExcPatternList(List<Pattern> urlExcPatternList) {
        this.urlExcPatternList = urlExcPatternList;
    }

    public void setUrlExcPatternList(String[] urlExcPatternValueList) {
        setUrlExcPatternList(stringArrayToPatternList(urlExcPatternValueList));
    }

    private String[] patternToStringArray(List<Pattern> patternList) {
        return patternList.stream().map(p -> p.pattern()).toArray(String[]::new);
    }

    private List<Pattern> stringArrayToPatternList(String[] urlIncPatternValueList) {
        List<Pattern> patterns = new ArrayList<>();
        for (String patternValue : urlIncPatternValueList) {
            patterns.add(Pattern.compile(patternValue));
        }
        return patterns;
    }

    public List<String> getMethodList() {
        return methodList;
    }

    public List<Integer> getCodeList() {
        return codeList;
    }

    public List<String> getRiskList() {
        return riskList;
    }

    public List<String> getConfidenceList() {
        return confidenceList;
    }

    public List<String> getTagList() {
        return tagList;
    }

    public String getNote() {
        return note;
    }
}
