// Name: Rahul Bhardwaj, Matriculation No.: 237868
package org.example;

public enum ComponentType {
    BB1,
    BB2,
    BB3,
    BB4,
    EB1,  // = (BB1, BB2)
    EB2,  // = (EB1, BB2)
    EB3,  // = (BB3, EB2)
    EB4,  // = (EB1, EB3)
    EB5   // = (EB3, EB4)
}
