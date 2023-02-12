/*
	A ZAP standalone script for testing Automation Framework (AF) context support.
	The script loops through a set of context files and for each:
		* Loads the context file
		* Create an automation plan based on it
		* Deletes the context
		* Runs the plan (which creates the context again)
		* Exports the context as a file
		* Deletes the context
	
	An external script can then diff the files to make sure the AF has (re)generated then correctly.
*/
// Default directories - can be changed for local testing
var base = '/zap/wrk/configs/plans/contexts'
var results = '/zap/wrk/output/'

var AutomationPlan = Java.type("org.zaproxy.addon.automation.AutomationPlan");
var ExtFile = Java.type("java.io.File");
var File = Java.type("java.io.File");

var dir = new File(base);
var files = dir.listFiles();

for (var i in files) {
	try {
		// Output the full file name for info
		print("Loading: " + files[i].getAbsolutePath());
	
		// Import the context file
		var context = model.getSession().importContext(files[i])
		var contextName = context.getName();
	
		// Create the plan
		var plan = new AutomationPlan();
		plan.getEnv().addContext(context);
	
		// Register then plan
		var extAuto = control.getExtensionLoader().getExtension("ExtensionAutomation");
		extAuto.registerPlan(plan);

		// Delete the existing context
		model.getSession().deleteContext(context)
	
		// Run the plan, which should recreate the context
		var progress = extAuto.runPlan(plan, true);
		if (progress.hasErrors()) {
			print(progress.getErrors());
		}
		if (progress.hasWarnings()) {
			print(progress.getWarnings());
		}
	
		// Export the context - must have the same name as the file
		context = model.getSession().getContext(contextName);
		var outFile = new File(results + contextName);
		print("Generating: " + outFile.getAbsolutePath());
		model.getSession().exportContext(context, outFile);
	
		// Tidy up
		model.getSession().deleteContext(context);
	}
	catch (err) {
		print("FAILED processing: " + files[i].getAbsolutePath());
		err.printStackTrace();
	}
}