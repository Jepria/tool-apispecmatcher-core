package org.jepria.tools.apispecmatcher.core;

public interface JaxrsMethod extends Method {

  ResponseBodySchemaExtractionStatus responseBodySchemaExtractionStatus();

  enum ResponseBodySchemaExtractionStatus {
    /**
     * The method return type declared is not a basic {@code javax.ws.rs.core.Response}
     */
    METHOD_RETURN_TYPE_DECLARED,

    /**
     * The method return type declared is a basic {@code javax.ws.rs.core.Response},
     * and no source tree provided to attempt static response body type extraction
     */
    STATIC_NO_SOURCE_TREE,

    /**
     * The method return type declared is a basic {@code javax.ws.rs.core.Response},
     * the source tree is provided, but no source file found in it for the class
     * containing the method
     */
    STATIC_NO_SOURCE_FILE,

    /**
     * The method return type declared is a basic {@code javax.ws.rs.core.Response},
     * the source tree is provided, the source file found for the class containing the method,
     * but no such method found in the source code
     */
    STATIC_NO_SOURCE_METHOD,

    /**
     * The method return type declared is a basic {@code javax.ws.rs.core.Response},
     * the source tree is provided, the source file found for the class containing the method,
     * the method found in the source code,
     * but no "responseBody" variable declaration found in the method body
     */
    STATIC_VARIABLE_UNDECLARED,

    /**
     * The method return type declared is a basic {@code javax.ws.rs.core.Response},
     * the source tree is provided, the source file found for the class containing the method,
     * the method found in the source code,
     * the "responseBody" variable declaration found in the method body
     */
    STATIC_VARIABLE_DECLARED,
  }
}
