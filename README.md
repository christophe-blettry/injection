# injection


A lightweight java injection system

#The Injectors

By meeting newbies engineer using Spring injectors, I wanted to understand the interest of injections and their implementations.
I asked for some explanation about injection, but their explanations have not convinced me.
So I tried to understand how the injections can be.

Java provide an annotation about injectors : [`@Inject`](http://docs.oracle.com/javaee/7/api/javax/inject/Inject.html)
 
I have found easily two methods:

- directly: by field modification
- by hooking method calls

but the third (by constructor) has been another story.

The first one uses reflection : 

- create a new instance of object with Class.newInstance()
- setting field of this newly created object

The second one uses dynamic proxy:

- we intercept the call of the method and we do something

For the constructor, I did not found any hooking methods inside the jdk.
In parallel I had to work around the APM (Application Performance Management), detailing the quickstart I found one java commandline option __-javaagent__ which allowed me to understand how to inject by constructor.

In this parper I will explain how to implement theses three type of injections.

*__WARNING:__ the code is an example code and must not operated in the state*



##Directly


*Simple usage of Reflection*

####Configuration

I will used a xml configuration file for pojos loading. It is [those classes](https://github.com/christophe-blettry/injection/tree/master/src/main/java/io/cb/java/injection/pojo) who loads those pojos (`Pojos.java, Pojo.java, Property.java`).

####Loading
The class `Context.java`[\*](https://github.com/christophe-blettry/injection/blob/master/src/main/java/io/cb/java/injection/pojo/Context.java) allows objects loading by their pojo configuration.
	  `Context.loadResource(String ... xml)` loads xml pojos configuration.  
	  `Context.getResource(String id)` loads the object with it's pojo configuration, the object class is configure by the attribute __class__ of the pojo element.
Let note the cast upon the generic type `T`  ( `return (T) getResource(pojo,classe)` ) for type inferance (we return the object correctly typed:  `public <T> T getResource (String id)` ) 

`Context.getResource(Pojo pojo, Class<T> classe)` returns the instance of the object loaded from the pojo configuration. It instantiates a new object  (`T instance =classe.newInstance()` ) by it's empty constructor and each field of this object is set by the correspondent pojo property.


##Method

*Use of Dynamics Proxy*

The class `java.lang.reflect.Proxy`[\*](https://docs.oracle.com/javase/8/docs/api/java/lang/reflect/Proxy.html)  can intercept the call to the methods of an interface.
To do this we must implement the interface `InvocationHandler`. The instantiation of this class is passed as a parameter to the proxy.
And the proxy returns an object that will implement the interface without having to write a class that implements it.
And each method call this interface will be proxifying (basically it is as if we directly instantiate an interface).



##Constructor

*Byte code modification ([javassist](http://jboss-javassist.github.io/javassist/) or [asm](http://asm.ow2.org/))*


This last point brought me more trouble, knowing that I have not found any entry point to the jdk to "hook" a constructor.

To do this we have to "instrument" the code (`java.lang.instrument`[\*](https://docs.oracle.com/javase/8/docs/api/java/lang/instrument/package-summary.html)) . It is possible to "transform" a class (`ClassFileTransformer`[\*](https://docs.oracle.com/javase/8/docs/api/java/lang/instrument/ClassFileTransformer.html)).

To instrument we must implement an agent. There are two ways to call the agent either from the command line either by calling the agent silently in the code.
This call is done thanks to a class `VirtualMachine`[\*](http://docs.oracle.com/javase/8/docs/jdk/api/attach/spec/com/sun/tools/attach/VirtualMachine.html) that belongs to a particular jar of jdk  : __tools.jar__ (in JAVA_HOME/lib).
It must be given a jar whose manifest contains an entry `Agent-Class` that corresponds to a class with `public static void agentmain(String agentArguments, Instrumentation instr)` method inside.

This method add a `ClassFileTransformer`[\*](https://docs.oracle.com/javase/8/docs/api/java/lang/instrument/ClassFileTransformer.html) 
we had the pleasure to implement (method: `byte[] 	transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)`).

It is this method that intercepts the class to its first instantiation, and that can replace one of your convenience.
But it must have the same signature, so you can't call another one. This mean you have to change it on the fly.

Tools allow you to modify the bytecode of a class (javassit, ASM). I used [javassist](http://jboss-javassist.github.io/javassist/) for the demo.


[Here the example](https://github.com/christophe-blettry/injectionexample)