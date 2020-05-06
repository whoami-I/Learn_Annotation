package com.example.ioc_compiler;

import com.example.ioc_annotation.Hello;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class HelloAnnotaionProcessor extends AbstractProcessor {
    private Elements elements;//辅助工具类
    private Filer filer;//用来写入文件用

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        System.out.println("#############################");
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "======>start");
        elements = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "======>start");
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new HashSet<>();
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "======>start");
        annotations.add(Hello.class.getCanonicalName());
        return annotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        System.out.println("#############################");
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "======>start");
        this.processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "======>start");
        Set<TypeElement> typeElements = (Set<TypeElement>) roundEnvironment.getElementsAnnotatedWith(Hello.class);;
        for (TypeElement typeElement : typeElements) {
            System.out.println("#######" + typeElement.getSimpleName());
            System.out.println("#######" + typeElement.getEnclosingElement().asType());
            System.out.println("#######" + typeElement.getQualifiedName());
            System.out.println("#######" + typeElement.getSuperclass().toString());
            this.processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "======>start");
            this.processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "======>start");
            String clsNmae = typeElement.getSimpleName().toString();
            String msg = typeElement.getAnnotation(Hello.class).value();
            MethodSpec main = MethodSpec.methodBuilder("main")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(void.class)
                    //.addParameter(String[].class, "args")
                    .addStatement("$T.out.println($S)", System.class, clsNmae + "-" + msg)
                    .build();

            // 创建HelloWorld类
            TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(main)
                    .build();

            try {
                // 生成 com.wzgiceman.viewinjector.HelloWorld.java
                JavaFile javaFile = JavaFile.builder("com.example.myapplication", helloWorld)
                        .addFileComment(" This codes are generated automatically. Do not modify!")
                        .build();
                //　生成文件
                javaFile.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

}
