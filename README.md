Пример запуска из Windows/cmd
```
java -cp "C:\work\tool-apispec-matcher\target\apispec-matcher-1.0.0-SNAPSHOT.jar;C:\work\bin-ext\build\com\google\code\gson\2.2.4\gson-2.2.4.jar;C:\work\bin-ext\build\com\github\javaparser\javaparser-core\3.2.5\javaparser-core-3.2.5.jar" org.jepria.tools.apispecmatcher.Main --api-spec "C:\work\tool-apispec-matcher\src\test\swagger.json" --jaxrs-adapter "C:\work\tool-apispec-matcher\src\test\FeatureJaxrsAdapter.java;C:\work\tool-apispec-matcher\src\test\FeatureProcessJaxrsAdapter.java"
```