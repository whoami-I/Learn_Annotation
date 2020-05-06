package com.example.ioc_compiler;

import com.example.ioc_annotation.BindView;
import com.example.ioc_annotation.Hello;
import com.example.ioc_annotation.IBindHelper;
import com.example.ioc_annotation.OnClick;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class BindAnnotation extends AbstractProcessor {
    Map<TypeElement, Set<ViewInfo>> bindViewMap = new HashMap<>();
    private Elements mElementsUtils;  // Element处理工具类
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mElementsUtils = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new HashSet<>();
        annotations.add(BindView.class.getCanonicalName());
        annotations.add(OnClick.class.getCanonicalName());
        return annotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set != null && set.size() != 0) {
            Set<? extends Element> bindViewElements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
            Set<? extends Element> onClickElements = roundEnvironment.getElementsAnnotatedWith(OnClick.class);
            categoryBindView(bindViewElements);
            categoryOnClick(onClickElements);
            Set<TypeElement> typeElements = bindViewMap.keySet();
            for (TypeElement typeElement : typeElements) {
                String className = typeElement.getSimpleName().toString();
                String packageName = mElementsUtils.getPackageOf(typeElement).getQualifiedName().toString();
                Set<ViewInfo> viewInfos = bindViewMap.get(typeElement);
                MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("inject").addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(void.class)
                        .addParameter(Object.class, "target")
                        .addCode(typeElement.getQualifiedName()+" a = ("+typeElement.getQualifiedName()+")target;");
                        //.addStatement("$T a = ($T)target", typeElement.asType().getKind(), typeElement.asType().getClass());

                for (ViewInfo viewInfo : viewInfos) {
                    StringBuilder sb = new StringBuilder();
                    //使用findviewbyid来找到相应的view
                    methodBuilder.addCode("a." + viewInfo.name + " = a.findViewById(" + viewInfo.id + ");");
                    ;
                }
                MethodSpec build = methodBuilder.build();
                // 创建类
                TypeSpec classZ = TypeSpec.classBuilder(className + "$$Bind")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addSuperinterface(IBindHelper.class)
                        .addMethod(build)
                        .build();

                try {
                    // 生成 com.wzgiceman.viewinjector.HelloWorld.java
                    JavaFile javaFile = JavaFile.builder(packageName, classZ)
                            .addFileComment(" This codes are generated automatically. Do not modify!")
                            .build();
                    //　生成文件
                    javaFile.writeTo(filer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return true;
    }

    private void categoryOnClick(Set<? extends Element> onClickElements) {
    }

    private void categoryBindView(Set<? extends Element> bindViewElements) {
        for (Element bindViewElement : bindViewElements) {
            TypeElement enclosingElement = (TypeElement) bindViewElement.getEnclosingElement();
            Set<ViewInfo> viewInfos = bindViewMap.get(enclosingElement);
            if (viewInfos == null) {
                viewInfos = new HashSet<>();
                bindViewMap.put(enclosingElement, viewInfos);
            }
            BindView annotation = bindViewElement.getAnnotation(BindView.class);
            int value = annotation.value();
            String name = bindViewElement.getSimpleName().toString();
            viewInfos.add(new ViewInfo(value, name));
        }
    }

    private static class ViewInfo {
        private int id;
        private String name;

        public ViewInfo(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
