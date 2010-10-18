1.0 HOW TO USE THIS TEMPLATE
======================================================================================
This template can be used as a base for creating addons for the Vaadin Directory. This template
includes tools for building your project, building the manual and unit testing your component.


1.1 FOLDERS
========================================================================================
The project contains three source folders which you should add to your project build path. 

- src : 	The main source folder for your component. Add 

- demo : 	The demo sources. When releasing a component it is preferred that you include a short
		 	demo application of your component so other developers can see how the component is 
		 	used. The demo will be built to a WAR when executing the build.xml script.
		 
- tests: 	Add your unit and other tests here. Remember to set the directory name of the main
			source package in build.properties (test.src.dir).
			
1.2 BUILD PROPERTIES
=========================================================================================
To successfully build your addon package you need to specify the correct properties for your addon
in the build.properties file before building. Most of the settings has been pre-filled for you but 
the sections marked with <...> you should fill in yourself.

To disable a build target, e.g. to disable manual building, uncomment to appropriate run.no-* flag.

1.3 BUILDING
=========================================================================================
To build the project run the Ant target 'all' which will build all targets except the disabled ones.
Note that for building the manual target you will need an Open Office installation and .odt version
of the manual in place.
