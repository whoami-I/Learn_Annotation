package com.example.ioc_compiler;

import com.example.ioc_annotation.BindView;
import com.example.ioc_annotation.Hello;
import com.example.ioc_annotation.IBindHelper;
import com.example.ioc_annotation.OnClick;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
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
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class BindAnnotation extends AbstractProcessor {
    private static final ClassName VIEW = ClassName.get("android.view", "View");


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
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
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
            generateCode();
        }
        return true;
    }

    private void generateCode() {
        Set<TypeElement> typeElements = bindViewMap.keySet();
        for (TypeElement typeElement : typeElements) {
            String className = typeElement.getSimpleName().toString();
            String packageName = mElementsUtils.getPackageOf(typeElement).getQualifiedName().toString();
            Set<ViewInfo> viewInfos = bindViewMap.get(typeElement);
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("inject").addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(void.class)
                    .addParameter(Object.class, "target")
                    //.addCode(typeElement.getQualifiedName()+" a = ("+typeElement.getQualifiedName()+")target;\n");
                    .addStatement("$L a = ($L)target", typeElement.getQualifiedName(), typeElement.getQualifiedName());
            //使用findviewbyid来找到相应的view
            methodBuilder.addStatement("$T view = null", VIEW);
            for (ViewInfo viewInfo : viewInfos) {
                methodBuilder.addStatement("view = a.findViewById($L)", viewInfo.id);
                if (viewInfo.name != null) {
                    //绑定view
                    methodBuilder.addStatement("a.$L = ($L)view", viewInfo.name,(ClassName)(viewInfo.typeName));
                }
                if (viewInfo.methodName != null) {
                    //绑定click
                    methodBuilder.addStatement("view.setOnClickListener(new View.OnClickListener() {\n" +
                            "            @Override\n" +
                            "            public void onClick(View v) {\n" +
                            "               a.$L(v);\n" +
                            "                \n" +
                            "            }\n" +
                            "        });", viewInfo.methodName);
                }
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

    private void categoryOnClick(Set<? extends Element> onClickElements) {
        for (Element onClickElement : onClickElements) {
            TypeElement enclosingElement = (TypeElement) onClickElement.getEnclosingElement();
            Set<ViewInfo> viewInfos = bindViewMap.get(enclosingElement);
            if (viewInfos == null) {
                viewInfos = new HashSet<>();
                bindViewMap.put(enclosingElement, viewInfos);
            }
            OnClick annotation = onClickElement.getAnnotation(OnClick.class);
            int value = annotation.value();
            String name = onClickElement.getSimpleName().toString();
            ViewInfo info = null;
            for (ViewInfo viewInfo : viewInfos) {
                if (viewInfo.id == value) {
                    info = viewInfo;
                    break;
                }
            }
            if (info == null) {
                info = new ViewInfo();
                info.id = value;
                viewInfos.add(info);
            }
            info.methodName = name;
        }
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
            ViewInfo info = null;
            for (ViewInfo viewInfo : viewInfos) {
                if (viewInfo.id == value) {
                    info = viewInfo;
                    break;
                }
            }
            if (info == null) {
                info = new ViewInfo();
                info.id = value;
                viewInfos.add(info);
            }
            info.name = name;
            info.typeName=TypeName.get(bindViewElement.asType());
        }
    }

    private static class ViewInfo {
        private int id;
        //变量的类型
        private TypeName typeName;
        //变量的名称
        private String name;
        private String methodName;


        public ViewInfo() {
        }
    }
}
