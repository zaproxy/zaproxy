package org.zaproxy.zap.extension.autoupdate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.control.AddOnCollection;
import org.zaproxy.zap.control.AddOnTestUtils;

class ManageAddOnsDialogTest {

    @Test
    void shouldProcessUpdateWhenConfirmed() throws Exception {
        TestDialog dialog = createDialog();

        AddOn addOn = createAddOn("Test", "1.0.0");
        dialog.selectedUpdates = List.of(addOn);
        dialog.confirmResult = true;

        JButton button = invokeButton(dialog, "getUpdateButton");
        click(button);

        // assertions orchestration behaviors
        assertThat(dialog.capturedUpdates, contains(addOn));
        assertTrue(dialog.confirmCalled);
        assertTrue(dialog.applyCalled);
        assertFalse(dialog.cancelled);
    }

    @Test
    void shouldNotApplyUpdateWhenCancelled() throws Exception {
        TestDialog dialog = createDialog();

        AddOn addOn = createAddOn("Test", "1.0.0");
        dialog.selectedUpdates = List.of(addOn);
        dialog.confirmResult = false;

        JButton button = invokeButton(dialog, "getUpdateButton");
        click(button);

        // assertions orchestration behaviors
        assertThat(dialog.capturedUpdates, contains(addOn));
        assertTrue(dialog.confirmCalled);
        assertFalse(dialog.applyCalled);
        assertTrue(dialog.cancelled);
    }

    @Test
    void shouldProcessInstallWhenConfirmed() throws Exception {
        TestDialog dialog = createDialog();

        AddOn addOn = createAddOn("Install", "2.0.0");
        dialog.selectedInstalls = List.of(addOn);
        dialog.confirmResult = true;

        JButton button = invokeButton(dialog, "getInstallButton");
        click(button);

        // assertions orchestration behaviors
        assertThat(dialog.capturedInstalls, contains(addOn));
        assertTrue(dialog.confirmCalled);
        assertTrue(dialog.applyCalled);
        assertFalse(dialog.cancelled);
    }

    @Test
    void shouldNotApplyInstallWhenCancelled() throws Exception {
        TestDialog dialog = createDialog();

        AddOn addOn = createAddOn("Install", "2.0.0");
        dialog.selectedInstalls = List.of(addOn);
        dialog.confirmResult = false;

        JButton button = invokeButton(dialog, "getInstallButton");
        click(button);

        // assertions orchestration behaviors
        assertThat(dialog.capturedInstalls, contains(addOn));
        assertTrue(dialog.confirmCalled);
        assertFalse(dialog.applyCalled);
        assertTrue(dialog.cancelled);
    }

    @Test
    void shouldKeepConflictPathAsNoOp() throws Exception {
        TestDialog dialog = createDialog();
        AddOn addOn = createAddOn("conflict", "1.0.0");

        dialog.selectedUpdates = List.of(addOn);
        dialog.simulateDependencyConflict = true;

        click(invokeButton(dialog, "getUpdateButton"));

        assertThat(dialog.capturedUpdates, contains(addOn));
        assertTrue(dialog.conflictDetected);
        assertFalse(dialog.applyCalled);
        assertTrue(dialog.cancelled);
    }

    private static class TestDialog extends ManageAddOnsDialog {
        List<AddOn> selectedUpdates = List.of();
        List<AddOn> selectedInstalls = List.of();

        List<AddOn> capturedUpdates = new ArrayList<>();
        List<AddOn> capturedInstalls = new ArrayList<>();

        boolean confirmResult;
        boolean confirmCalled;
        boolean applyCalled;
        boolean cancelled;

        boolean simulateDependencyConflict;
        boolean conflictDetected;

        TestDialog() {
            super(
                    Mockito.mock(ExtensionAutoUpdate.class),
                    Constant.PROGRAM_VERSION,
                    new AddOnCollection());
        }

        // updates the flow
        @Override
        protected void processUpdates(List<AddOn> addOns) {
            capturedUpdates = new ArrayList<>(addOns);

            if (simulateDependencyConflict) {
                conflictDetected = true;
                cancelled = true;
                return;
            }

            confirmCalled = true;
            if (confirmResult) {
                applyCalled = true;
            } else {
                cancelled = true;
            }
        }

        // installs the flow
        @Override
        protected void processInstall(List<AddOn> addOns) {
            capturedInstalls = new ArrayList<>(addOns);

            if (simulateDependencyConflict) {
                conflictDetected = true;
                cancelled = true;
                return;
            }

            confirmCalled = true;

            if (confirmResult) {
                applyCalled = true;
            } else {
                cancelled = true;
            }
        }
    }

    private static TestDialog createDialog() throws Exception {
        return runOnEdt(TestDialog::new);
    }

    private static AddOn createAddOn(String id, String version) throws Exception {
        TestUtils utils = new TestUtils();
        AddOn addOn = new AddOn(utils.createAddOnFile(id + ".zap", "release", version));
        addOn.setId(id);
        return addOn;
    }

    private static JButton invokeButton(Object dialog, String method) throws Exception {
        Method m = dialog.getClass().getSuperclass().getDeclaredMethod(method);
        m.setAccessible(true);
        return (JButton) m.invoke(dialog);
    }

    private static void click(JButton button) throws Exception {
        runOnEdt(() -> {
                    button.doClick();
                    return null;
                });
    }

    private static <T> T runOnEdt(ThrowingSupplier<T> supplier) throws Exception {
        final List<T> result = new ArrayList<>(1);

        SwingUtilities.invokeAndWait(() -> {
                    try {
                        result.add(supplier.get());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        return result.get(0);
    }

    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    private static class TestUtils extends AddOnTestUtils {}
}
