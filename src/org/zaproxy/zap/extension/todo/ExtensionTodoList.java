package org.zaproxy.zap.extension.todo;



import java.util.ResourceBundle;



import org.parosproxy.paros.Constant;

import org.parosproxy.paros.extension.ExtensionAdaptor;

import org.parosproxy.paros.extension.ExtensionHook;



public class ExtensionTodoList extends ExtensionAdaptor{



	private TodoList todoList = null;

    private ResourceBundle messages = null;

    

	@Override

	public String getAuthor() {

		return "vishesh";

	}



	public ExtensionTodoList(){

		super();

		initialize();

		

	}

	

	public ExtensionTodoList(String name){

		super(name);

	}

	/**

	 * This method initializes this

	 * 

	 */

	private void initialize() {

        this.setName("ExtensionTodoList");

        

        // Load extension specific language files - these are held in the extension jar

        messages = ResourceBundle.getBundle(

        		this.getClass().getPackage().getName() + ".Messages", Constant.getLocale());

	

       

	}

	

	private TodoList getTodoList(){

		if(todoList==null){

			todoList = new TodoList();

			todoList.setName("Todo List");

		}

		return todoList;

	}

	@Override

	public void hook(ExtensionHook extensionHook) {

	    super.hook(extensionHook);

	    

	    if (getView() != null) {

	    	extensionHook.getHookView().addWorkPanel(getTodoList());

	    	

	    }



	}

}
