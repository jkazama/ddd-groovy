package sample.context

import groovy.transform.*
import groovy.util.logging.Slf4j

import org.springframework.stereotype.*
import org.springframework.web.bind.annotation.RestController

/** 静的コンパイル概念を持ったComponent */
@Component
@AnnotationCollector([CompileStatic, Slf4j])
@interface StaticComponent {}

/** 静的コンパイル概念を持ったService */
@Service
@AnnotationCollector([CompileStatic, Slf4j])
@interface StaticService {}

/** 静的コンパイル概念を持ったRestController */
@RestController
@AnnotationCollector([CompileStatic, Slf4j])
@interface RestStaticController {}

/** 静的コンパイル概念を持ったDto */
@AnnotationCollector([CompileStatic, Canonical])
@interface StaticDto {}
