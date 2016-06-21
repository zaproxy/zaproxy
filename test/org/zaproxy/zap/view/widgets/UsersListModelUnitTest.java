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
package org.zaproxy.zap.view.widgets;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ListDataListener;

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
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.utils.I18N;
import org.zaproxy.zap.view.ListModelTestUtils;

/**
 * Unit test for {@code UsersListModel}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Constant.class)
public class UsersListModelUnitTest extends ListModelTestUtils {

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
    public void shouldFailToCreateInstanceWithUndefinedUsersTableModel() {
        // Given
        UsersTableModel undefinedTableModel = null;
        // When
        UsersListModel usersListModel = new UsersListModel(undefinedTableModel);
        // Then = NullPointerException
    }

    @Test
    public void shouldAddListDataListeners() {
        // Given
        ListDataListener listener = createTestListDataListener();
        UsersListModel usersListModel = new UsersListModel(createUsersTableModel(0));
        // When
        usersListModel.addListDataListener(listener);
        // Then
        assertThat(usersListModel.getListDataListeners(), is(arrayContaining(listener)));
    }

    @Test
    public void shouldRemoveListDataListeners() {
        // Given
        TestListDataListener listener = createTestListDataListener();
        UsersListModel usersListModel = new UsersListModel(createUsersTableModel(0));
        usersListModel.addListDataListener(listener);
        // When
        usersListModel.removeListDataListener(listener);
        // Then
        assertThat(usersListModel.getListDataListeners(), is(emptyArray()));
    }

    @Test
    public void shouldGetNullElementIfNoTableUsersNorCustomUsers() {
        // Given
        UsersListModel usersListModel = new UsersListModel(createUsersTableModel(0));
        // When
        User user = usersListModel.getElementAt(0);
        // Then
        assertThat(user, is(nullValue()));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void shouldFailToGetCustomUserIfIndexIsMoreThanAvailableCustomUsers() {
        // Given
        UsersListModel usersListModel = new UsersListModel(createUsersTableModel(0));
        usersListModel.setCustomUsers(new User[] { createUser(), createUser() });
        // When
        User user = usersListModel.getElementAt(2);
        // Then = ArrayIndexOutOfBoundsException
    }

    @Test
    public void shouldGetTableUsers() {
        // Given
        UsersTableModel tableModel = createUsersTableModel(2);
        UsersListModel usersListModel = new UsersListModel(tableModel);
        // When
        User user = usersListModel.getElementAt(0);
        // Then
        assertThat(user, is(equalTo(tableModel.getUsers().get(0))));
    }

    @Test
    public void shouldSetCustomUsersEvenWithoutExistingTableUsers() {
        // Given
        TestListDataListener listener = createTestListDataListener();
        UsersListModel usersListModel = new UsersListModel(createUsersTableModel(0));
        usersListModel.addListDataListener(listener);
        User customUser1 = createUser();
        User customUser2 = createUser();
        User[] customUsers = new User[] { customUser1, customUser2 };
        // When
        usersListModel.setCustomUsers(customUsers);
        // Then
        assertThat(usersListModel.getElementAt(0), is(equalTo(customUser1)));
        assertThat(usersListModel.getElementAt(1), is(equalTo(customUser2)));
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isListItemAdded(0), is(equalTo(true)));
        assertThat(listener.isListItemAdded(1), is(equalTo(true)));
        assertThat(listener.isListItemAdded(2), is(equalTo(false)));
    }

    @Test
    public void shouldSetCustomUsersEvenWithExistingTableUsers() {
        // Given
        TestListDataListener listener = createTestListDataListener();
        UsersListModel usersListModel = new UsersListModel(createUsersTableModel(2));
        usersListModel.addListDataListener(listener);
        User customUser1 = createUser();
        User customUser2 = createUser();
        User[] customUsers = new User[] { customUser1, customUser2 };
        // When
        usersListModel.setCustomUsers(customUsers);
        // Then
        assertThat(usersListModel.getElementAt(2), is(equalTo(customUser1)));
        assertThat(usersListModel.getElementAt(3), is(equalTo(customUser2)));
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isListItemAdded(1), is(equalTo(false)));
        assertThat(listener.isListItemAdded(2), is(equalTo(true)));
        assertThat(listener.isListItemAdded(3), is(equalTo(true)));
        assertThat(listener.isListItemAdded(4), is(equalTo(false)));
    }

    @Test
    public void shouldReplaceCustomUsersEvenWithoutExistingTableUsers() {
        // Given
        UsersListModel usersListModel = new UsersListModel(createUsersTableModel(0));
        usersListModel.setCustomUsers(new User[] { createUser(), createUser(), createUser() });
        TestListDataListener listener = createTestListDataListener();
        usersListModel.addListDataListener(listener);
        User customUser1 = createUser();
        User customUser2 = createUser();
        User[] customUsers = new User[] { customUser1, customUser2 };
        // When
        usersListModel.setCustomUsers(customUsers);
        // Then
        assertThat(usersListModel.getElementAt(0), is(equalTo(customUser1)));
        assertThat(usersListModel.getElementAt(1), is(equalTo(customUser2)));
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isListItemChanged(0), is(equalTo(true)));
        assertThat(listener.isListItemChanged(1), is(equalTo(true)));
        assertThat(listener.isListItemChanged(2), is(equalTo(true)));
    }

    @Test
    public void shouldReplaceCustomUsersEvenWithExistingTableUsers() {
        // Given
        UsersListModel usersListModel = new UsersListModel(createUsersTableModel(2));
        usersListModel.setCustomUsers(new User[] { createUser(), createUser(), createUser() });
        TestListDataListener listener = createTestListDataListener();
        usersListModel.addListDataListener(listener);
        User customUser1 = createUser();
        User customUser2 = createUser();
        User[] customUsers = new User[] { customUser1, customUser2 };
        // When
        usersListModel.setCustomUsers(customUsers);
        // Then
        assertThat(usersListModel.getElementAt(2), is(equalTo(customUser1)));
        assertThat(usersListModel.getElementAt(3), is(equalTo(customUser2)));
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isListItemChanged(1), is(equalTo(false)));
        assertThat(listener.isListItemChanged(2), is(equalTo(true)));
        assertThat(listener.isListItemChanged(3), is(equalTo(true)));
        assertThat(listener.isListItemChanged(4), is(equalTo(true)));
    }

    @Test
    public void shouldHaveNoEffectToSetNoCustomUsersIfNoneExist() {
        // Given
        UsersListModel usersListModel = new UsersListModel(createUsersTableModel(0));
        TestListDataListener listener = createTestListDataListener();
        usersListModel.addListDataListener(listener);
        // When
        usersListModel.setCustomUsers(null);
        // Then
        assertThat(listener.getNumberOfEvents(), is(equalTo(0)));
    }

    @Test
    public void shouldHaveNoEffectToSetNoCustomUsersIfNoneWerePreviouslySet() {
        // Given
        UsersListModel usersListModel = new UsersListModel(createUsersTableModel(0));
        usersListModel.setCustomUsers(new User[] {});
        TestListDataListener listener = createTestListDataListener();
        usersListModel.addListDataListener(listener);
        // When
        usersListModel.setCustomUsers(null);
        // Then
        assertThat(listener.getNumberOfEvents(), is(equalTo(0)));
    }

    @Test
    public void shouldRemoveCustomUsersEvenWithoutExistingTableUsers() {
        // Given
        UsersListModel usersListModel = new UsersListModel(createUsersTableModel(0));
        usersListModel.setCustomUsers(new User[] { createUser(), createUser() });
        TestListDataListener listener = createTestListDataListener();
        usersListModel.addListDataListener(listener);
        // When
        usersListModel.setCustomUsers(null);
        // Then
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isListItemRemoved(0), is(equalTo(true)));
        assertThat(listener.isListItemRemoved(1), is(equalTo(true)));
        assertThat(listener.isListItemRemoved(2), is(equalTo(false)));
    }

    @Test
    public void shouldRemoveCustomUsersEvenWithExistingTableUsers() {
        // Given
        UsersListModel usersListModel = new UsersListModel(createUsersTableModel(2));
        usersListModel.setCustomUsers(new User[] { createUser(), createUser() });
        TestListDataListener listener = createTestListDataListener();
        usersListModel.addListDataListener(listener);
        // When
        usersListModel.setCustomUsers(null);
        // Then
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isListItemRemoved(1), is(equalTo(false)));
        assertThat(listener.isListItemRemoved(2), is(equalTo(true)));
        assertThat(listener.isListItemRemoved(3), is(equalTo(true)));
        assertThat(listener.isListItemRemoved(4), is(equalTo(false)));
    }

    @Test
    public void shouldRemoveCustomUsersWithEmptyArrayEvenWithExistingTableUsers() {
        // Given
        UsersListModel usersListModel = new UsersListModel(createUsersTableModel(2));
        usersListModel.setCustomUsers(new User[] { createUser(), createUser() });
        TestListDataListener listener = createTestListDataListener();
        usersListModel.addListDataListener(listener);
        // When
        usersListModel.setCustomUsers(new User[] {});
        // Then
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isListItemRemoved(1), is(equalTo(false)));
        assertThat(listener.isListItemRemoved(2), is(equalTo(true)));
        assertThat(listener.isListItemRemoved(3), is(equalTo(true)));
        assertThat(listener.isListItemRemoved(4), is(equalTo(false)));
    }

    @Test
    public void shouldGetSizeAsNumberOfTableUsersIfNoCustomUsers() {
        // Given
        UsersListModel usersListModel = new UsersListModel(createUsersTableModel(3));
        // When
        int size = usersListModel.getSize();
        // Then
        assertThat(size, is(equalTo(3)));
    }

    @Test
    public void shouldGetSizeAsNumberOfTableUsersAndCustomUsersSet() {
        // Given
        UsersListModel usersListModel = new UsersListModel(createUsersTableModel(3));
        usersListModel.setCustomUsers(new User[] { createUser(), createUser() });
        // When
        int size = usersListModel.getSize();
        // Then
        assertThat(size, is(equalTo(5)));
    }

    @Test
    public void shouldGetSizeAsNumberOfCustomUsersIfNoTableUsers() {
        // Given
        UsersListModel usersListModel = new UsersListModel(createUsersTableModel(0));
        usersListModel.setCustomUsers(new User[] { createUser(), createUser() });
        // When
        int size = usersListModel.getSize();
        // Then
        assertThat(size, is(equalTo(2)));
    }

    @Test
    public void shouldGetInvalidIndexIfUserNotFound() {
        // Given
        UsersListModel usersListModel = new UsersListModel(createUsersTableModel(0));
        // When
        int index = usersListModel.getIndexOf(createUser());
        // Then
        assertThat(index, is(equalTo(-1)));
    }

    @Test
    public void shouldGetIndexOfTableUsers() {
        // Given
        UsersTableModel tableModel = createUsersTableModel(2);
        UsersListModel usersListModel = new UsersListModel(tableModel);
        // When
        int index = usersListModel.getIndexOf(tableModel.getUsers().get(1));
        // Then
        assertThat(index, is(equalTo(1)));
    }

    @Test
    public void shouldGetIndexOfCustomUsersWithoutTableUsers() {
        // Given
        UsersListModel usersListModel = new UsersListModel(createUsersTableModel(0));
        User customUser = createUser();
        usersListModel.setCustomUsers(new User[] { customUser, createUser() });
        // When
        int index = usersListModel.getIndexOf(customUser);
        // Then
        assertThat(index, is(equalTo(0)));
    }

    @Test
    public void shouldGetIndexOfCustomUsersWithTableUsers() {
        // Given
        UsersListModel usersListModel = new UsersListModel(createUsersTableModel(2));
        User customUser = createUser();
        usersListModel.setCustomUsers(new User[] { customUser, createUser() });
        // When
        int index = usersListModel.getIndexOf(customUser);
        // Then
        assertThat(index, is(equalTo(2)));
    }

    @Test
    public void shouldNotHaveSelectedUserByDefault() {
        // Given
        UsersListModel usersListModel = new UsersListModel(createUsersTableModel(1));
        // When
        Object user = usersListModel.getSelectedItem();
        // Then
        assertThat(user, is(nullValue()));
    }

    @Test
    public void shouldNotSetSelectedItemIfNotFound() {
        // Given
        UsersListModel usersListModel = new UsersListModel(createUsersTableModel(0));
        TestListDataListener listener = createTestListDataListener();
        usersListModel.addListDataListener(listener);
        // When
        usersListModel.setSelectedItem(createUser());
        // Then
        assertThat(usersListModel.getSelectedItem(), is(nullValue()));
        assertThat(listener.getNumberOfEvents(), is(equalTo(0)));
        assertThat(listener.isListItemChanged(-1), is(equalTo(false)));
    }

    @Test
    public void shouldNotSetSelectedItemIfNotAUser() {
        // Given
        UsersListModel usersListModel = new UsersListModel(createUsersTableModel(0));
        TestListDataListener listener = createTestListDataListener();
        usersListModel.addListDataListener(listener);
        // When
        usersListModel.setSelectedItem(new Object());
        // Then
        assertThat(usersListModel.getSelectedItem(), is(nullValue()));
        assertThat(listener.getNumberOfEvents(), is(equalTo(0)));
        assertThat(listener.isListItemChanged(-1), is(equalTo(false)));
    }

    @Test
    public void shouldSetSelectedItemUser() {
        // Given
        UsersTableModel tableModel = createUsersTableModel(2);
        UsersListModel usersListModel = new UsersListModel(tableModel);
        TestListDataListener listener = createTestListDataListener();
        usersListModel.addListDataListener(listener);
        // When
        usersListModel.setSelectedItem(tableModel.getUsers().get(1));
        // Then
        assertThat(usersListModel.getSelectedItem(), is(equalTo((Object) tableModel.getUsers().get(1))));
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isListItemChanged(-1), is(equalTo(true)));
    }

    @Test
    public void shouldHaveNoEffectReselectSameUser() {
        // Given
        UsersTableModel tableModel = createUsersTableModel(2);
        UsersListModel usersListModel = new UsersListModel(tableModel);
        usersListModel.setSelectedItem(tableModel.getUsers().get(1));
        TestListDataListener listener = createTestListDataListener();
        usersListModel.addListDataListener(listener);
        // When
        usersListModel.setSelectedItem(tableModel.getUsers().get(1));
        // Then
        assertThat(usersListModel.getSelectedItem(), is(equalTo((Object) tableModel.getUsers().get(1))));
        assertThat(listener.getNumberOfEvents(), is(equalTo(0)));
        assertThat(listener.isListItemChanged(-1), is(equalTo(false)));
    }

    @Test
    public void shouldSetSelectedItemUserEvenIfOneWasPreviouslySelected() {
        // Given
        UsersTableModel tableModel = createUsersTableModel(2);
        UsersListModel usersListModel = new UsersListModel(tableModel);
        usersListModel.setSelectedItem(tableModel.getUsers().get(1));
        TestListDataListener listener = createTestListDataListener();
        usersListModel.addListDataListener(listener);
        // When
        usersListModel.setSelectedItem(tableModel.getUsers().get(0));
        // Then
        assertThat(usersListModel.getSelectedItem(), is(equalTo((Object) tableModel.getUsers().get(0))));
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isListItemChanged(-1), is(equalTo(true)));
    }

    @Test
    public void shouldRemoveSelectionWithSelectedItem() {
        // Given
        UsersTableModel tableModel = createUsersTableModel(2);
        UsersListModel usersListModel = new UsersListModel(tableModel);
        usersListModel.setSelectedItem(tableModel.getUsers().get(1));
        TestListDataListener listener = createTestListDataListener();
        usersListModel.addListDataListener(listener);
        // When
        usersListModel.setSelectedItem(null);
        // Then
        assertThat(usersListModel.getSelectedItem(), is(nullValue()));
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isListItemChanged(-1), is(equalTo(true)));
    }

    @Test
    public void shouldHaveNoEffectRemoveSelectionIfNoSelectedUser() {
        // Given
        UsersTableModel tableModel = createUsersTableModel(2);
        UsersListModel usersListModel = new UsersListModel(tableModel);
        TestListDataListener listener = createTestListDataListener();
        usersListModel.addListDataListener(listener);
        // When
        usersListModel.setSelectedItem(null);
        // Then
        assertThat(usersListModel.getSelectedItem(), is(nullValue()));
        assertThat(listener.getNumberOfEvents(), is(equalTo(0)));
        assertThat(listener.isListItemChanged(-1), is(equalTo(false)));
    }

    @Test
    public void shouldRemoveSelectionIfNoUsersAndSetSelectedInternalUserIsNotFound() {
        // Given
        UsersListModel usersListModel = new UsersListModel(createUsersTableModel(0));
        TestListDataListener listener = createTestListDataListener();
        usersListModel.addListDataListener(listener);
        // When
        usersListModel.setSelectedInternalItem(createUser());
        // Then
        assertThat(usersListModel.getSelectedItem(), is(nullValue()));
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isListItemChanged(-1), is(equalTo(true)));
    }

    @Test
    public void shouldFallbackToFirstExistingUserIfSetSelectedInternalUserIsNotFound() {
        // Given
        UsersTableModel tableModel = createUsersTableModel(2);
        UsersListModel usersListModel = new UsersListModel(tableModel);
        TestListDataListener listener = createTestListDataListener();
        usersListModel.addListDataListener(listener);
        // When
        usersListModel.setSelectedInternalItem(createUser());
        // Then
        assertThat(usersListModel.getSelectedItem(), is(equalTo((Object) tableModel.getUsers().get(0))));
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isListItemChanged(-1), is(equalTo(true)));
    }

    @Test
    public void shouldSetSelectedInternalTableUsers() {
        // Given
        UsersTableModel tableModel = createUsersTableModel(2);
        UsersListModel usersListModel = new UsersListModel(tableModel);
        TestListDataListener listener = createTestListDataListener();
        usersListModel.addListDataListener(listener);
        // When
        usersListModel.setSelectedInternalItem(copyUser(tableModel.getUsers().get(1)));
        // Then
        assertThat(usersListModel.getSelectedItem(), is(equalTo((Object) tableModel.getUsers().get(1))));
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isListItemChanged(-1), is(equalTo(true)));
    }

    @Test
    public void shouldSetSelectedInternalCustomUsersWithoutTableUsers() {
        // Given
        UsersListModel usersListModel = new UsersListModel(createUsersTableModel(0));
        User customUser = createUser();
        usersListModel.setCustomUsers(new User[] { customUser, createUser() });
        TestListDataListener listener = createTestListDataListener();
        usersListModel.addListDataListener(listener);
        // When
        usersListModel.setSelectedInternalItem(copyUser(customUser));
        // Then
        assertThat(usersListModel.getSelectedItem(), is(equalTo((Object) customUser)));
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isListItemChanged(-1), is(equalTo(true)));
    }

    @Test
    public void shouldSetSelectedInternalCustomUsersWithTableUsers() {
        // Given
        UsersListModel usersListModel = new UsersListModel(createUsersTableModel(2));
        User customUser = createUser();
        usersListModel.setCustomUsers(new User[] { customUser, createUser() });
        TestListDataListener listener = createTestListDataListener();
        usersListModel.addListDataListener(listener);
        // When
        usersListModel.setSelectedInternalItem(copyUser(customUser));
        // Then
        assertThat(usersListModel.getSelectedItem(), is(equalTo((Object) customUser)));
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isListItemChanged(-1), is(equalTo(true)));
    }

    // TODO test tableChanged

    @Test
    public void shouldReflectAddedTableUserAndSelectFirstTableUserIfNoneSelected() {
        // Given
        UsersTableModel tableModel = createUsersTableModel(2);
        UsersListModel usersListModel = new UsersListModel(tableModel);
        TestListDataListener listener = createTestListDataListener();
        usersListModel.addListDataListener(listener);
        User user = createUser();
        // When
        tableModel.addElement(user);
        // Then
        assertThat(usersListModel.getSize(), is(equalTo(3)));
        assertThat(usersListModel.getElementAt(2), is(equalTo((Object) user)));
        assertThat(usersListModel.getSelectedItem(), is(equalTo((Object) tableModel.getUsers().get(0))));
        assertThat(listener.getNumberOfEvents(), is(equalTo(2)));
        assertThat(listener.isListItemAdded(2), is(equalTo(true)));
        assertThat(listener.isListItemChanged(-1), is(equalTo(true)));
    }

    @Test
    public void shouldReflectAddedTableUserAndNotSelectFirstTableUserIfOneAlreadySelected() {
        // Given
        UsersTableModel tableModel = createUsersTableModel(2);
        UsersListModel usersListModel = new UsersListModel(tableModel);
        usersListModel.setSelectedItem(tableModel.getUsers().get(0));
        TestListDataListener listener = createTestListDataListener();
        usersListModel.addListDataListener(listener);
        User user = createUser();
        // When
        tableModel.addElement(user);
        // Then
        assertThat(usersListModel.getSize(), is(equalTo(3)));
        assertThat(usersListModel.getElementAt(2), is(equalTo((Object) user)));
        assertThat(usersListModel.getSelectedItem(), is(equalTo((Object) tableModel.getUsers().get(0))));
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isListItemAdded(2), is(equalTo(true)));
    }

    @Test
    public void shouldReflectChangesToTableUsersAndSelectFirstTableUserIfNoneSelected() {
        // Given
        UsersTableModel tableModel = createUsersTableModel(2);
        UsersListModel usersListModel = new UsersListModel(tableModel);
        TestListDataListener listener = createTestListDataListener();
        usersListModel.addListDataListener(listener);
        User user = createUser();
        // When
        tableModel.modifyElement(1, user);
        // Then
        assertThat(usersListModel.getElementAt(1), is(equalTo((Object) user)));
        assertThat(usersListModel.getSelectedItem(), is(equalTo((Object) tableModel.getUsers().get(0))));
        assertThat(listener.getNumberOfEvents(), is(equalTo(2)));
        assertThat(listener.isListItemChanged(1), is(equalTo(true)));
        assertThat(listener.isListItemChanged(-1), is(equalTo(true)));
    }

    @Test
    public void shouldReflectChangesToTableUsersAndNotSelectFirstTableUserIfOneAlreadySelected() {
        // Given
        UsersTableModel tableModel = createUsersTableModel(3);
        UsersListModel usersListModel = new UsersListModel(tableModel);
        usersListModel.setSelectedItem(tableModel.getUsers().get(2));
        TestListDataListener listener = createTestListDataListener();
        usersListModel.addListDataListener(listener);
        User user = createUser();
        // When
        tableModel.modifyElement(1, user);
        // Then
        assertThat(usersListModel.getElementAt(1), is(equalTo((Object) user)));
        assertThat(usersListModel.getSelectedItem(), is(equalTo((Object) tableModel.getUsers().get(2))));
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isListItemChanged(1), is(equalTo(true)));
    }

    @Test
    public void shouldReflectChangesToSelectedTableUser() {
        // Given
        UsersTableModel tableModel = createUsersTableModel(2);
        UsersListModel usersListModel = new UsersListModel(tableModel);
        usersListModel.setSelectedItem(tableModel.getUsers().get(1));
        TestListDataListener listener = createTestListDataListener();
        usersListModel.addListDataListener(listener);
        User user = copyUser(tableModel.getUsers().get(1));
        // When
        tableModel.modifyElement(1, user);
        // Then
        assertThat(usersListModel.getElementAt(1), is(equalTo((Object) user)));
        assertThat(usersListModel.getSelectedItem(), is(equalTo((Object) user)));
        assertThat(usersListModel.getSelectedItem(), is(sameInstance((Object) user)));
        assertThat(listener.getNumberOfEvents(), is(equalTo(2)));
        assertThat(listener.isListItemChanged(1), is(equalTo(true)));
    }

    @Test
    public void shouldReflectClearedTableUsersAndRemoveSelectionOfClearedUser() {
        // Given
        UsersTableModel tableModel = createUsersTableModel(2);
        UsersListModel usersListModel = new UsersListModel(tableModel);
        usersListModel.setSelectedItem(tableModel.getUsers().get(1));
        TestListDataListener listener = createTestListDataListener();
        usersListModel.addListDataListener(listener);
        User user = createUser();
        // When
        tableModel.clear();
        // Then
        assertThat(usersListModel.getSize(), is(equalTo(0)));
        assertThat(usersListModel.getSelectedItem(), is(nullValue()));
        assertThat(listener.getNumberOfEvents(), is(equalTo(2)));
        assertThat(listener.isListItemChanged(0), is(equalTo(true)));
        assertThat(listener.isListItemChanged(1), is(equalTo(true)));
        assertThat(listener.isListItemChanged(-1), is(equalTo(true)));
    }

    @Test
    public void shouldReflectClearedTableUsersAndSelectFirstCustomUser() {
        // Given
        UsersTableModel tableModel = createUsersTableModel(2);
        UsersListModel usersListModel = new UsersListModel(tableModel);
        usersListModel.setSelectedItem(tableModel.getUsers().get(1));
        User customUser = createUser();
        usersListModel.setCustomUsers(new User[] { customUser, createUser() });
        TestListDataListener listener = createTestListDataListener();
        usersListModel.addListDataListener(listener);
        // When
        tableModel.clear();
        // Then
        assertThat(usersListModel.getSize(), is(equalTo(2)));
        assertThat(usersListModel.getSelectedItem(), is(equalTo((Object) customUser)));
        assertThat(listener.getNumberOfEvents(), is(equalTo(2)));
        assertThat(listener.isListItemChanged(0), is(equalTo(true)));
        assertThat(listener.isListItemChanged(1), is(equalTo(true)));
        assertThat(listener.isListItemChanged(-1), is(equalTo(true)));
    }

    @Test
    public void shouldReflectClearedTableUsersAndNotChangeSelectionIfNoUserSelectedNorAvailable() {
        // Given
        UsersTableModel tableModel = createUsersTableModel(2);
        UsersListModel usersListModel = new UsersListModel(tableModel);
        TestListDataListener listener = createTestListDataListener();
        usersListModel.addListDataListener(listener);
        User user = createUser();
        // When
        tableModel.clear();
        // Then
        assertThat(usersListModel.getSize(), is(equalTo(0)));
        assertThat(usersListModel.getSelectedItem(), is(nullValue()));
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isListItemChanged(0), is(equalTo(true)));
        assertThat(listener.isListItemChanged(1), is(equalTo(true)));
        assertThat(listener.isListItemChanged(-1), is(equalTo(false)));
    }

    @Test
    public void shouldReflectRemovedTableUserAndRemoveSelectionOfClearedUserToAvailableUser() {
        // Given
        UsersTableModel tableModel = createUsersTableModel(2);
        UsersListModel usersListModel = new UsersListModel(tableModel);
        usersListModel.setSelectedItem(tableModel.getUsers().get(1));
        TestListDataListener listener = createTestListDataListener();
        usersListModel.addListDataListener(listener);
        User user = createUser();
        // When
        tableModel.removeElement(1);
        // Then
        assertThat(usersListModel.getSize(), is(equalTo(1)));
        assertThat(usersListModel.getSelectedItem(), is(equalTo((Object) tableModel.getUsers().get(0))));
        assertThat(listener.getNumberOfEvents(), is(equalTo(2)));
        assertThat(listener.isListItemRemoved(1), is(equalTo(true)));
        assertThat(listener.isListItemChanged(-1), is(equalTo(true)));
    }

    @Test
    public void shouldReflectRemovedTableUserAndSelectFirstCustomUser() {
        // Given
        UsersTableModel tableModel = createUsersTableModel(1);
        UsersListModel usersListModel = new UsersListModel(tableModel);
        usersListModel.setSelectedItem(tableModel.getUsers().get(0));
        User customUser = createUser();
        usersListModel.setCustomUsers(new User[] { customUser, createUser() });
        TestListDataListener listener = createTestListDataListener();
        usersListModel.addListDataListener(listener);
        // When
        tableModel.removeElement(0);
        // Then
        assertThat(usersListModel.getSize(), is(equalTo(2)));
        assertThat(usersListModel.getSelectedItem(), is(equalTo((Object) customUser)));
        assertThat(listener.getNumberOfEvents(), is(equalTo(2)));
        assertThat(listener.isListItemRemoved(0), is(equalTo(true)));
        assertThat(listener.isListItemChanged(-1), is(equalTo(true)));
    }

    @Test
    public void shouldReflectRemovedTableUserAndNotChangeSelectionIfNotTheSelectedUser() {
        // Given
        UsersTableModel tableModel = createUsersTableModel(2);
        UsersListModel usersListModel = new UsersListModel(tableModel);
        usersListModel.setSelectedItem(tableModel.getUsers().get(1));
        TestListDataListener listener = createTestListDataListener();
        usersListModel.addListDataListener(listener);
        User user = createUser();
        // When
        tableModel.removeElement(0);
        // Then
        assertThat(usersListModel.getSize(), is(equalTo(1)));
        assertThat(usersListModel.getSelectedItem(), is(equalTo((Object) tableModel.getUsers().get(0))));
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isListItemRemoved(0), is(equalTo(true)));
        assertThat(listener.isListItemChanged(-1), is(equalTo(false)));
    }

    @Test
    public void shouldReflectRemovedTableUserAndNotChangeSelectionIfNoUserSelectedNorAvailable() {
        // Given
        UsersTableModel tableModel = createUsersTableModel(1);
        UsersListModel usersListModel = new UsersListModel(tableModel);
        TestListDataListener listener = createTestListDataListener();
        usersListModel.addListDataListener(listener);
        User user = createUser();
        // When
        tableModel.removeElement(0);
        // Then
        assertThat(usersListModel.getSize(), is(equalTo(0)));
        assertThat(usersListModel.getSelectedItem(), is(nullValue()));
        assertThat(listener.getNumberOfEvents(), is(equalTo(1)));
        assertThat(listener.isListItemRemoved(0), is(equalTo(true)));
        assertThat(listener.isListItemChanged(-1), is(equalTo(false)));
    }

    private static UsersTableModel createUsersTableModel(int numberOfUsers) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < numberOfUsers; i++) {
            users.add(createUser("User " + i));
        }
        return new UsersTableModel(users);
    }

    private static User copyUser(User user) {
        return new User(user.getContextId(), user.getName(), user.getId());
    }

    private static User createUser() {
        return createUser("User");
    }

    private static User createUser(String name) {
        return new User(1, name);
    }
}
