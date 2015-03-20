package net.mentalarray.doozie.Internal;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by kdivincenzo on 11/5/14.
 */
public class InternalClassLoader {

    private static final Class[] parameters = new Class[]{URL.class};

    protected static void addFile(String s) throws IOException {
        File f = new File(s);
        addFile(f);
    }//end method

    protected static void addFile(File f) throws IOException {
        addURL(f.toURI().toURL());
    }//end method


    protected static void addURL(URL u) throws IOException {

        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class sysclass = URLClassLoader.class;

        try {
            Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(sysloader, new Object[]{u});
        } catch (Throwable t) {
            t.printStackTrace();
            throw new IOException("Error, could not add URL to system ClassLoader.", t);
        }//end try catch

    }//end method
}
