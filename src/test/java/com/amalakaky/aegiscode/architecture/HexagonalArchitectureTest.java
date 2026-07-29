package com.amalakaky.aegiscode.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.amalakaky.aegiscode")
public class HexagonalArchitectureTest {

  @ArchTest
  static final ArchRule domain_should_be_isolated =
      noClasses()
          .that().resideInAPackage("..domain..")
          .should().dependOnClassesThat().resideInAnyPackage(
              "..infrastructure..",
              "..application..",
              "org.springframework.."
          )
          .because("El Dominio (DDD) debe ser agnóstico a frameworks "
              + "e infraestructura externa.");

  @ArchTest
  static final ArchRule infrastructure_should_not_depend_on_application_implementation =
      noClasses()
          .that().resideInAPackage("..infrastructure..")
          .should().dependOnClassesThat().resideInAPackage("..application.usecase..")
          .because("La infraestructura solo debe interactuar con la aplicación "
              + "a través de Puertos (Interfaces).");
}