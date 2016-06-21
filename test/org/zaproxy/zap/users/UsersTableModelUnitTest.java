/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2016 The ZAP Development Team
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
package org.zaproxy.zap.users;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.Whitebox;
import org.parosproxy.paros.Constant;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.zaproxy.zap.extension.users.UsersTableModel;
import org.zaproxy.zap.utils.I18N;
import org.zaproxy.zap.view.TableModelTestUtils;

/**
 * Unit test for {@code UsersTableModel}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Constant.class)
public class UsersTableModelUnitTest extends TableModelTestUtils {

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockConstantClass();
    }

    private void mockConstantClass() {
        Constant constant = PowerMockito.mock(Constant.class);
        I18N i18n = PowerMockito.mock(I18N.class);
        given(i18n.getString(anyString())).willReturn("");
        given(i18n.getString(anyString(), anyObject())).willReturn("");
        Whitebox.setInternalState(constant, "messages", i18n);
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailToCreateInstanceWithUndefinedUsersList() {
        // Given
        List<User> undefinedUsersList = null;
        // When
        UsersTableModel usersTableModel = new UsersTableModel(undefinedUsersList);
        // Then = NullPointerException
    }

    @Test
    public void shouldAddTableModelListeners() {
        // Given
        TableModelListener listener = createTestTableModelListener();
        UsersTableModel usersTableModel = new UsersTableModel();
        // When
        usersTableModel.addTableModelListener(listener);
        // Then
        assertThat(usersTableModel.getTableModelListeners(), is(arrayContaining(listener)));
    }

    @Test
    public void shouldRemoveTableModelListeners() {
        // Given
        TableModelListener listener = createTestTableModelListener();
        UsersTableModel usersTableModel = new UsersTableModel();
        usersTableModel.addTableModelListener(listener);
        // When
        usersTableModel.removeTableModelListener(listener);
        // Then
        assertThat(usersTableModel.getTableModelListeners(), is(emptyArray()));
    }

    @Test
    public void shouldHaveJustThreeColumns() {
        // Given
        UsersTableModel usersTableModel = new UsersTableModel();
        // When / Then
        assertThat(usersTableModel.getColumnCount(), is(equalTo(3)));
        assertThat(usersTableModel.getColumnName(0), is(notNullValue()));
        assertThat(usersTableModel.getColumnName(1), is(notNullValue()));
        assertThat(usersTableModel.getColumnName(2), is(notNullValue()));
    }

    @Test
    public void shouldBeEditableJustTheFirstColumnEvenIfHasNoUsers() {
        // Given
        UsersTableModel usersTableModel = new UsersTableModel();
        // When / Then
        assertThat(usersTableModel.isCellEditable(0, 0), is(equalTo(true)));
        assertThat(usersTableModel.isCellEditable(0, 1), is(equalTo(false)));
    }

    @Test
    public void shouldNotHaveUsersByDefault() {
        // Given
        UsersTableModel usersTableModel = new UsersTableModel();
        // When / Then
        assertThat(usersTableModel.getRowCount(), is(equalTo(0)));
        assertThat(usersTableModel.getUsers(), is(empty()));
        assertThat(usersTableModel.getElements(), is(empty()));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldFailToGetValueOfNonExistingRow() {
        // Given
        UsersTableModel usersTableModel = new UsersTableModel();
        // When
        usersTableModel.getValueAt(0, 0);
        // Then = IndexOutOfBoundsException
    }

    @Test
    public void shouldCreateInstanceWithUsersList() {
        // Given
        List<User> usersList = new ArrayList<>();
        usersList.add(createUser());
        usersList.add(createUser());
        usersList.add(createUser());
        // When
        UsersTableModel usersTableModel = new UsersTableModel(usersList);
        // Then
        assertThat(usersTableModel.getRowCount(), is(equalTo(usersList.size())));
        assertThat(usersTableModel.getUsers(), is(equalTo(usersList)));
        assertThat(usersTableModel.getElements(), is(equalTo(usersList)));
    }

    @Test
    public void shouldReturnUserEnabledStateFromFirstColumn() {
        // Given
        List<User> usersList = new ArrayList<>();
        usersList.add(createUser());
        usersList.add(createEnabledUser());
        // When
        UsersTableModel usersTableModel = new UsersTableModel(usersList);
        // Then
        assertTrue(usersTableModel.getColumnClass(0) == Boolean.class);
        assertThat(usersTableModel.getValueAt(0, 0), is(equalTo((Object) false)));
        assertThat(usersTableModel.getValueAt(1, 0), is(equalTo((Object) true)));
    }

    @Test
    public void shouldReturnUserIdFromSecondColumn() {
        // Given
        List<User> usersList = new ArrayList<>();
        User user1 = createUser();
        int user1Id = user1.getId();
        usersList.add(user1);
        User user2 = createUser();
        int user2Id = user2.getId();
        usersList.add(user2);
        // When
        UsersTableModel usersTableModel = new UsersTableModel(usersList);
        // Then
        assertTrue(usersTableModel.getColumnClass(1) == Integer.class);
        assertThat(usersTableModel.getValueAt(0, 1), is(equalTo((Object) Integer.valueOf(user1Id))));
        assertThat(usersTableModel.getValueAt(1, 1), is(equalTo((Object) Integer.valueOf(user2Id))));
    }

    @Test
    public void shouldReturnUserNameFromThirdColumn() {
        // Given
        List<User> usersList = new ArrayList<>();
        usersList.add(createUser("User 1"));
        usersList.add(createUser("User 2"));
        // When
        UsersTableModel usersTableModel = new UsersTableModel(usersList);
        // Then
        assertTrue(usersTableModel.getColumnClass(2) == String.class);
        assertThat(usersTableModel.getValueAt(0, 2), is(equalTo((Object) "User 1")));
        assertThat(usersTableModel.getValueAt(1, 2), is(equalTo((Object) "User 2")));
    }

    @Test
    public void shouldReturnNullValueAndColumnClassForNonExistingColumns() {
        // Given
        List<User> usersList = new ArrayList<>();
        usersList.add(createUser());
        // When
        UsersTableModel usersTableModel = new UsersTableModel(usersList);
        // Then
        assertThat(usersTableModel.getColumnClass(3), is(equalTo(null)));
        assertThat(usersTableModel.getValueAt(0, 3), is(equalTo(null)));
    }

    @Test
    public void shouldChangeUsersEnabledState() {
        // Given
        TestTableModelListener listener = createTestTableModelListener();
        List<User> usersList = new ArrayList<>();
        usersList.add(createUser());
        usersList.add(createEnabledUser());
        UsersTableModel usersTableModel = new UsersTableModel(usersList);
        usersTableModel.addTableModelListener(listener);
        // When
        usersTableModel.setValueAt(true, 0, 0);
        usersTableModel.setValueAt(false, 1, 0);
        // Then
        assertThat(usersTableModel.getValueAt(0, 0), is(equalTo((Object) true)));
        assertThat(usersTableModel.getValueAt(1, 0), is(equalTo((Object) false)));
        assertThat(listener.getNumberOfEvents(), is(equalTo(2)));
        assertThat(listener.isCellChanged(0, 0), is(equalTo(true)));
        assertThat(listener.isCellChanged(1, 0), is(equalTo(true)));
    }

    @Test
    public void shouldNotChangeUsersEnabledStateIfNonBooleanOrFirstColumn() {
        // Given
        TestTableModelListener listener = createTestTableModelListener();
        List<User> usersList = new ArrayList<>();
        usersList.add(createUser());
        usersList.add(createEnabledUser());
        UsersTableModel usersTableModel = new UsersTableModel(usersList);
        usersTableModel.addTableModelListener(listener);
        // When
        usersTableModel.setValueAt(Integer.valueOf(15), 0, 0);
        usersTableModel.setValueAt("Some Value", 1, 0);
        usersTableModel.setValueAt(true, 1, 1);
        // Then
        assertThat(listener.getNumberOfEvents(), is(equalTo(0)));
        assertThat(usersTableModel.getValueAt(0, 0), is(equalTo((Object) false)));
        assertThat(usersTableModel.getValueAt(1, 0), is(equalTo((Object) true)));
    }

    @Test
    public void shouldEnableAllUsers() {
        // Given
        TestTableModelListener listener = createTestTableModelListener();
        List<User> usersList = new ArrayList<>();
        usersList.add(createUser());
        usersList.add(createEnabledUser());
        UsersTableModel usersTableModel = new UsersTableModel(usersList);
        usersTableModel.addTableModelListener(listener);
        // When
        usersTableModel.setAllEnabled(true);
        // Then
        assertThat(usersTableModel.getValueAt(0, 0), is(equalTo((Object) true)));
        assertThat(usersTableModel.getValueAt(1, 0), is(equalTo((Object) true)));
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isCellChanged(0, 0), is(equalTo(true)));
        assertThat(listener.isCellChanged(1, 0), is(equalTo(true)));
    }

    @Test
    public void shouldDisableAllUsers() {
        // Given
        TestTableModelListener listener = createTestTableModelListener();
        List<User> usersList = new ArrayList<>();
        usersList.add(createUser());
        usersList.add(createEnabledUser());
        UsersTableModel usersTableModel = new UsersTableModel(usersList);
        usersTableModel.addTableModelListener(listener);
        // When
        usersTableModel.setAllEnabled(false);
        // Then
        assertThat(usersTableModel.getValueAt(0, 0), is(equalTo((Object) false)));
        assertThat(usersTableModel.getValueAt(1, 0), is(equalTo((Object) false)));
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isCellChanged(0, 0), is(equalTo(true)));
        assertThat(listener.isCellChanged(1, 0), is(equalTo(true)));
    }

    @Test
    public void shouldNotNotifyOfEnabledStateChangesIfItHasNoUsers() {
        // Given
        TestTableModelListener listener = createTestTableModelListener();
        UsersTableModel usersTableModel = new UsersTableModel();
        usersTableModel.addTableModelListener(listener);
        // When
        usersTableModel.setAllEnabled(true);
        // Then
        assertThat(listener.getNumberOfEvents(), is(equalTo(0)));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldFailToGetNonExistingElement() {
        // Given
        UsersTableModel usersTableModel = new UsersTableModel();
        // When
        usersTableModel.getElement(1);
        // Then = IndexOutOfBoundsException
    }

    @Test
    public void shouldAddElement() {
        // Given
        TestTableModelListener listener = createTestTableModelListener();
        UsersTableModel usersTableModel = new UsersTableModel();
        usersTableModel.addTableModelListener(listener);
        User user = createUser();
        // When
        usersTableModel.addElement(user);
        // Then
        assertThat(usersTableModel.getRowCount(), is(equalTo(1)));
        assertThat(usersTableModel.getElements().size(), is(equalTo(1)));
        assertThat(usersTableModel.getElements().get(0), is(equalTo(user)));
        assertThat(usersTableModel.getElement(0), is(equalTo(user)));
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isRowInserted(0), is(equalTo(true)));
    }

    @Test
    public void shouldAddUser() {
        // Given
        TestTableModelListener listener = createTestTableModelListener();
        UsersTableModel usersTableModel = new UsersTableModel();
        usersTableModel.addTableModelListener(listener);
        User user = createUser();
        // When
        usersTableModel.addUser(user);
        // Then
        assertThat(usersTableModel.getRowCount(), is(equalTo(1)));
        assertThat(usersTableModel.getUsers().size(), is(equalTo(1)));
        assertThat(usersTableModel.getUsers().get(0), is(equalTo(user)));
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isRowInserted(0), is(equalTo(true)));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldFailToModifyNonExistingElement() {
        // Given
        List<User> usersList = new ArrayList<>();
        usersList.add(createEnabledUser());
        UsersTableModel usersTableModel = new UsersTableModel(usersList);
        // When
        usersTableModel.modifyElement(1, createUser());
        // Then = IndexOutOfBoundsException
    }

    @Test
    public void shouldModifyElement() {
        // Given
        TestTableModelListener listener = createTestTableModelListener();
        List<User> usersList = new ArrayList<>();
        usersList.add(createEnabledUser());
        UsersTableModel usersTableModel = new UsersTableModel(usersList);
        usersTableModel.addTableModelListener(listener);
        User user2 = createUser("User 2");
        // When
        usersTableModel.modifyElement(0, user2);
        // Then
        assertThat(usersTableModel.getElements().size(), is(equalTo(1)));
        assertThat(usersTableModel.getElements().get(0), is(equalTo(user2)));
        assertThat(usersTableModel.getElement(0), is(equalTo(user2)));
        assertThat(usersTableModel.getUsers().size(), is(equalTo(1)));
        assertThat(usersTableModel.getUsers().get(0), is(equalTo(user2)));
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isRowUpdated(0), is(equalTo(true)));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldFailToRemoveNonExistingElement() {
        // Given
        UsersTableModel usersTableModel = new UsersTableModel();
        // When
        usersTableModel.removeElement(1);
        // Then = IndexOutOfBoundsException
    }

    @Test
    public void shouldRemoveElement() {
        // Given
        TestTableModelListener listener = createTestTableModelListener();
        List<User> usersList = new ArrayList<>();
        User user = createUser();
        usersList.add(user);
        usersList.add(createEnabledUser());
        UsersTableModel usersTableModel = new UsersTableModel(usersList);
        usersTableModel.addTableModelListener(listener);
        // When
        usersTableModel.removeElement(1);
        // Then
        assertThat(usersTableModel.getRowCount(), is(equalTo(1)));
        assertThat(usersTableModel.getElements().size(), is(equalTo(1)));
        assertThat(usersTableModel.getElements().get(0), is(equalTo(user)));
        assertThat(usersTableModel.getElement(0), is(equalTo(user)));
        assertThat(usersTableModel.getUsers().size(), is(equalTo(1)));
        assertThat(usersTableModel.getUsers().get(0), is(equalTo(user)));
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isRowRemoved(1), is(equalTo(true)));
    }

    @Test
    public void shouldClearElements() {
        // Given
        TestTableModelListener listener = createTestTableModelListener();
        List<User> usersList = new ArrayList<>();
        usersList.add(createUser());
        usersList.add(createEnabledUser());
        UsersTableModel usersTableModel = new UsersTableModel(usersList);
        usersTableModel.addTableModelListener(listener);
        // When
        usersTableModel.clear();
        // Then
        assertThat(usersTableModel.getRowCount(), is(equalTo(0)));
        assertThat(usersTableModel.getElements(), is(empty()));
        assertThat(usersTableModel.getUsers(), is(empty()));
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isDataChanged(), is(equalTo(true)));
    }

    @Test
    public void shouldRemoveAllUsers() {
        // Given
        TestTableModelListener listener = createTestTableModelListener();
        List<User> usersList = new ArrayList<>();
        usersList.add(createUser());
        usersList.add(createEnabledUser());
        UsersTableModel usersTableModel = new UsersTableModel(usersList);
        usersTableModel.addTableModelListener(listener);
        // When
        usersTableModel.removeAllUsers();
        // Then
        assertThat(usersTableModel.getRowCount(), is(equalTo(0)));
        assertThat(usersTableModel.getElements(), is(empty()));
        assertThat(usersTableModel.getUsers(), is(empty()));
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isDataChanged(), is(equalTo(true)));
    }

    @Test
    public void shouldSetUsers() {
        // Given
        TestTableModelListener listener = createTestTableModelListener();
        UsersTableModel usersTableModel = new UsersTableModel();
        usersTableModel.addTableModelListener(listener);
        List<User> usersList = new ArrayList<>();
        User user = createUser();
        usersList.add(user);
        // When
        usersTableModel.setUsers(usersList);
        // Then
        assertThat(usersTableModel.getRowCount(), is(equalTo(1)));
        assertThat(usersTableModel.getElements().size(), is(equalTo(1)));
        assertThat(usersTableModel.getElements().get(0), is(equalTo(user)));
        assertThat(usersTableModel.getElement(0), is(equalTo(user)));
        assertThat(usersTableModel.getUsers().size(), is(equalTo(1)));
        assertThat(usersTableModel.getUsers().get(0), is(equalTo(user)));
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isDataChanged(), is(equalTo(true)));
    }

    private static User createUser() {
        return createUser("User");
    }

    private static User createUser(String name) {
        return new User(1, name);
    }

    private User createEnabledUser() {
        User user = createUser();
        user.setEnabled(true);
        return user;
    }
}
