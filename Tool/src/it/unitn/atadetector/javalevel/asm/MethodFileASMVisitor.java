package it.unitn.atadetector.javalevel.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class will go through the methods invoked in a method of the .class file being analyzed
 */
public class MethodFileASMVisitor extends MethodVisitor {


    // this is the object that collects all the elements (strings, methods, fields, ...) found in the class
    private final ArrayList<HashMap<String, int[]>> classProperties;

    // the class name and the flag
    private String className;
    private String flag;

    /**
     * Constructor
     * @param api ASM4
     * @param className the class name
     * @param classProperties array where to put classes in [0], methods in [1], attributes in [2], strings in [3]
     * @param flag flag to insert along with the found occurrences
     */
    MethodFileASMVisitor(int api, String className, ArrayList<HashMap<String, int[]>> classProperties, String flag) {

        // super constructor
        super(api);

        // assign the class variables
        this.classProperties = classProperties;
        this.className = className;
        this.flag = flag;
    }

    // visit each method invoked in the .class method and add it into the proper set
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterface) {

        // OPTIMIZATION: if this is not an inner method
        if (!owner.equals(className)) {

            int[] oldClassValues = classProperties.get(0).getOrDefault(owner, new int[] {0, 0});
            int[] newClassValues = new int[] {oldClassValues[0] + 1, (flag.equals("1") ? oldClassValues[1] : oldClassValues[1] + 1)};
            classProperties.get(0).put(owner, newClassValues);

            int[] oldMethodValues = classProperties.get(1).getOrDefault(owner + "." + name, new int[] {0, 0});
            int[] newMethodValues = new int[] {oldMethodValues[0] + 1, (flag.equals("1") ? oldMethodValues[1] : oldMethodValues[1] + 1)};
            classProperties.get(1).put(owner + "." + name, newMethodValues);
        }
    }


    // for each field in the method
    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {

        // OPTIMIZATION: if this is not an inner field
        if (!owner.equals(className)) {

            int[] oldClassValues = classProperties.get(0).getOrDefault(owner, new int[] {0, 0});
            int[] newClassValues = new int[] {oldClassValues[0] + 1, (flag.equals("1") ? oldClassValues[1] : oldClassValues[1] + 1)};
            classProperties.get(0).put(owner, newClassValues);

            int[] oldMethodValues = classProperties.get(2).getOrDefault(owner + "." + name, new int[] {0, 0});
            int[] newMethodValues = new int[] {oldMethodValues[0] + 1, (flag.equals("1") ? oldMethodValues[1] : oldMethodValues[1] + 1)};
            classProperties.get(2).put(owner + "." + name, newMethodValues);
        }

    }


    // if there is an instruction to push a value on the stack
    @Override
    public void visitLdcInsn(Object value) {

        // if the value is not null
        if (value != null) {

            // if it is a string
            if (value instanceof String) {

                String valueAsString = (String) value;
                int[] oldValues = classProperties.get(3).getOrDefault(valueAsString, new int[]{0, 0});
                int[] newValues = new int[]{oldValues[0] + 1, (flag.equals("1") ? oldValues[1] : oldValues[1] + 1)};
                classProperties.get(3).put(valueAsString, newValues);
            }
        }
    }
}