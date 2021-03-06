package br.edu.archunit;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;

import org.junit.Test;

import br.edu.archunit.persistence.Dao;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

public class FooArchitectTest {

    JavaClasses importedClasses = new ClassFileImporter()
            .importPackages("br.edu.archunit");

    @Test
    public void verificarDependenciasParaCamadaNegocio() {
        ArchRule rule = classes()
                .that().resideInAPackage("..service..")
                .should().onlyHaveDependentClassesThat()
                .resideInAnyPackage("..business..");

        rule.check(importedClasses);
    }

    @Test
    public void verificarDependenciasParaCamadaPersistencia() {
        ArchRule rule = classes()
                .that().resideInAPackage("..persistence..")
                .should().onlyHaveDependentClassesThat()
                .resideInAnyPackage("..persistence..", "..service..");

        rule.check(importedClasses);
    }

    @Test
    public void verificarDependenciasDaCamadaPersistencia() {
        ArchRule rule = noClasses()
        .that().resideInAPackage("..persistence..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..service..");

        rule.check(importedClasses);
    }

    @Test
    public void verificarDependenciasDaCamadaService() {
        ArchRule rule = noClasses()
        .that().resideInAPackage("..service..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..business..");

        rule.check(importedClasses);
    }

    @Test
    public void verificarNomesClassesCamadaPersistencia() {
        ArchRule rule = classes()
        .that().haveSimpleNameEndingWith("Dao")
        .should().resideInAPackage("..persistence..");

        rule.check(importedClasses);
    }

    @Test
    public void verificarImplementacaoInterfaceDao() {
        ArchRule rule = classes()
        .that().implement(Dao.class)
        .should().haveSimpleNameEndingWith("Dao");

        rule.check(importedClasses);
    }

    @Test
    public void verificarDependenciasCiclicas() {
        ArchRule rule = slices()
        .matching("br.edu.archunit.(*)..")
        .should().beFreeOfCycles();
        rule.check(importedClasses);
    }

    @Test
    public void verificarViolacaoCamadas() {
        ArchRule rule = layeredArchitecture()
        .layer("Business").definedBy("..business..")
        .layer("Service").definedBy("..service..")
        .layer("Persistence").definedBy("..persistence..")
        .whereLayer("Service").mayOnlyBeAccessedByLayers("Business")
        .whereLayer("Persistence").mayOnlyBeAccessedByLayers("Service");
        rule.check(importedClasses);
    }
}
