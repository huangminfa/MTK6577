MTK SDK APIs
~~~~~~~~~~~~~


This directory contains a full MTK SDK API source codes (shared library) , 
that will not change the Android framework. 

To declare the shared library to the framework, you must place a file with 
a .xml extension in the /system/etc/permissions directory with the following 
contents:

<?xml version="1.0" encoding="utf-8"?>
<permissions>
    <library name="com.mediatek.framework"
            file="/system/framework/com.mediatek.framework.jar"/>
</permissions>



com.mediatek.framework
~~~~~~~~~~~~~~~~~~~~~~~~~

The top-level Android.mk defines the rules to build the shared library itself,
whose target is "com.mediatek.framework".  The code for this library lives 
under java/.

Note that the product for this library is a raw .jar file, NOT a .apk, which
means there is no manifest or resources associated with the library.
Unfortunately this means that if you need any resources for the library, such
as drawables or layout files, you will need to add these to the core framework
resources under frameworks/base/res or follow the MTK resource management 
mechanism. Please make sure when doing this that you do not make any of these 
resources public, they should not become part of the Android API.  In the future 
Google will allow shared libraries to have their own resources.

Other than that, the library is very straight-forward, and you can write
basically whatever code you want.  You can also put code in other Java
namespaces -- the namespace given in the <library> tag above is just the
public unique name by which clients will link to your library, but once this
link happens all of the Java namespaces in that library will be available
to the client.


Applications need to use this shared library
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The only two special things needed to use your library are:

- A LOCAL_JAVA_LIBRARIES line in the Android.mk to have the build system link
against your shared library.

- A <uses-library> line in the AndroidManifest.xml to have the runtime load
your library into the application.
