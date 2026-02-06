# Tests ArchUnit - Architecture Hexagonale

Ce répertoire contient les tests **ArchUnit** qui valident automatiquement le respect de l'architecture hexagonale et l'
isolation entre les modules du projet.

## 🎯 Objectif

Garantir que les modules `course`, `user` et `storage` restent **complètement isolés** et suivent strictement l'*
*architecture hexagonale**, permettant une future séparation en microservices.

## 📁 Fichiers

### `OnionRulesTests.java`

Classe de tests contenant 15 règles ArchUnit:

- ✅ Architecture hexagonale par module (3 tests)
- ✅ Isolation entre modules (6 tests)
- ✅ Pureté du domaine (2 tests)
- ✅ Découplage application/adapters (4 tests)

## 🚀 Exécution

### Tous les tests

```bash
./mvnw test -Dtest=OnionRulesTests
```

### Un test spécifique

```bash
./mvnw test -Dtest=OnionRulesTests#courseModuleShouldFollowHexagonalArchitecture
```

### Via script PowerShell

```bash
.\run-archunit-tests.ps1
.\run-archunit-tests.ps1 courseModuleShouldFollowHexagonalArchitecture
```

## 📊 Statut Actuel

**Résultats:** 8/15 tests réussis (53%)

### ✅ Tests Réussis

- Architecture hexagonale: Course ✅, Storage ✅
- Isolation: 4/6 règles respectées
- Domaine pur: 2/2 règles respectées
- Découplage: 2/4 règles respectées

### ❌ Tests Échoués

- User module: architecture non stricte (13 violations)
- Storage → Course: dépendances sur classes communes (5 violations)
- User → Course: dépendances sur classes communes (2 violations)

**Voir détails:** `ARCHUNIT_VIOLATIONS_REPORT.md` (racine du projet)

## 🔧 Corrections Nécessaires

### Priorité 1: Déplacer classes communes

Déplacer de `course.domain.common` vers `common.domain`:

- `Utils.java`
- `NodeIdValidationException.java`
- `EntityId.java`

### Priorité 2: Refactorer User module

- Utiliser des ports au lieu d'adapters directs
- Déplacer le mapping dans les adapters
- Utiliser des objets du domaine dans les ports

## 📚 Documentation

- **Guide complet:** `ARCHUNIT_DOCUMENTATION.md`
- **Rapport violations:** `ARCHUNIT_VIOLATIONS_REPORT.md`
- **Documentation officielle:** https://www.archunit.org/

## ✨ Bénéfices

Une fois tous les tests verts:

- ✅ Modules isolés (prêts pour microservices)
- ✅ Architecture validée automatiquement
- ✅ Pas de régression architecture possible
- ✅ Maintenance facilitée

## 🎓 Ressources

- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [ArchUnit User Guide](https://www.archunit.org/userguide/html/000_Index.html)

---

**Dernière mise à jour:** 2026-02-04  
**Mainteneur:** Équipe SkillsHub
