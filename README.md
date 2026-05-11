# Mi Ruta de Hábitos

Aplicación Android para registrar y completar hábitos personales. Construida con **Jetpack Compose**, **Material 3** y **Room** para persistencia local.

La lista de hábitos se mantiene aunque la app se cierre — los datos viven en una base SQLite local administrada por Room, y la UI se reactualiza automáticamente cuando la tabla cambia gracias a `Flow` + `collectAsState`.

---

## Stack

| Capa | Tecnología |
| --- | --- |
| Lenguaje | Kotlin 2.2.10 |
| UI | Jetpack Compose (BOM 2026.02.01) + Material 3 |
| Persistencia | Room 2.8.4 (SQLite) |
| Procesamiento de anotaciones | KSP 2.2.10-2.0.2 |
| Reactividad | Kotlin Coroutines + Flow |
| Build | Gradle 9 (AGP 9.2.0), Kotlin DSL |
| Min SDK / Target SDK | 24 / 36 |

---

## Arquitectura

Arquitectura intencionalmente simple, en una sola capa por encima de Room. No hay ViewModel, repositorio ni DI framework: el DAO se inyecta directamente al composable raíz.

```
┌────────────────────────┐    ┌────────────┐    ┌────────────┐
│  MiRutaHabitosApp      │───▶│  HabitoDao │───▶│ HabitoDB   │
│  (Compose UI)          │◀───│   (Flow)   │◀───│  (Room)    │
└────────────────────────┘    └────────────┘    └─────┬──────┘
                                                      ▼
                                                ┌──────────┐
                                                │  SQLite  │
                                                └──────────┘
```

Flujo de un cambio:

1. Usuario interactúa (escribe, marca checkbox, pulsa eliminar).
2. El composable dispara una corrutina (`scope.launch { ... }`) que invoca un método `suspend` del DAO.
3. Room ejecuta la operación en SQLite.
4. La `@Query` reactiva (`Flow<List<HabitoEntity>>`) emite la nueva lista.
5. `collectAsState` la entrega a Compose y la UI se recompone.

---

## Estructura de carpetas

```
My_routines/
├── app/
│   ├── build.gradle.kts                       # plugins + dependencias (Room, KSP)
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/my_routines/
│       │   ├── MainActivity.kt                # punto de entrada, crea la DB y monta la UI
│       │   ├── data/
│       │   │   ├── HabitoEntity.kt            # @Entity (tabla "habitos")
│       │   │   ├── HabitoDao.kt               # @Dao con Flow y operaciones suspend
│       │   │   └── HabitoDatabase.kt          # @Database + singleton
│       │   └── ui/
│       │       ├── MiRutaHabitosApp.kt        # composable raíz, consume el DAO
│       │       ├── HabitoCard.kt              # tarjeta reusable por hábito
│       │       └── theme/                     # Color, Theme, Type (Material 3)
│       └── res/                               # strings, themes, iconos
├── gradle/
│   └── libs.versions.toml                     # catálogo de versiones
├── build.gradle.kts                           # plugins a nivel de raíz
├── settings.gradle.kts
└── Path_to_follow/                            # PDFs de referencia que guiaron el diseño
```

---

## Setup local

### Requisitos

- **Android Studio Ladybug** o superior (con soporte AGP 9.x).
- **JDK 17** (o JDK 11 mínimo según `compileOptions`).
- **Android SDK 36** instalado (`compileSdk = 36`).
- Un emulador o dispositivo físico con **API 24+**.

### Pasos

1. Clonar el repositorio.
2. Abrir la carpeta `My_routines/` en Android Studio.
3. Esperar a que Gradle sincronice (descarga Room, KSP, Compose).
4. Conectar un emulador / dispositivo.
5. Pulsar **Run ▶** (o `./gradlew :app:installDebug` desde la CLI).

No hay variables de entorno requeridas. `local.properties` apunta al SDK local de Android Studio y es generado automáticamente.

### Comandos útiles

```bash
# Compilar APK debug
./gradlew :app:assembleDebug

# Instalar en dispositivo / emulador conectado
./gradlew :app:installDebug

# Lint
./gradlew :app:lintDebug

# Limpiar
./gradlew clean
```

---

## Módulos importantes

### `data/HabitoEntity.kt`
Define la tabla `habitos` con tres columnas: `id` (auto-generada), `nombre` y `completado`. Es un `data class` para aprovechar `copy()` al alternar el estado completado.

### `data/HabitoDao.kt`
Expone cuatro operaciones:

- `obtenerHabitos(): Flow<List<HabitoEntity>>` — consulta reactiva, emite cada vez que la tabla cambia.
- `insertarHabito(habito)` — `suspend`.
- `actualizarHabito(habito)` — `suspend`.
- `eliminarHabito(habito)` — `suspend`.

### `data/HabitoDatabase.kt`
Singleton con `@Volatile` + `synchronized` para asegurar una sola instancia. Crea/abre la base local `habitos_db` usando `applicationContext` para evitar fugas de Activity.

### `ui/MiRutaHabitosApp.kt`
Composable raíz. Mantiene un único estado local en memoria: el texto del `OutlinedTextField` (`nuevoHabito`). Todo lo demás vive en Room. Aplica dos validaciones antes de insertar:

- `trim().isNotBlank()` — descarta vacíos y solo-espacios.
- No-duplicado (case-insensitive) — `any { it.nombre.equals(texto, ignoreCase = true) }`.

### `ui/HabitoCard.kt`
Tarjeta reusable con nombre, estado textual, `Checkbox` para alternar completado y `TextButton` "Eliminar".

---

## Decisiones técnicas

| Decisión | Por qué |
| --- | --- |
| **Sin ViewModel ni Repository** | El alcance es pequeño, la UI sólo necesita un DAO. El PDF guía (`Path_to_follow/`) lo presenta como mejora futura, no como requisito. |
| **DAO inyectado directamente al composable** | Más explícito y simple que añadir Hilt/Koin para una sola dependencia. |
| **Singleton manual en `HabitoDatabase`** | Patrón estándar Room. Suficiente y libre de dependencias adicionales. |
| **`Flow<List<HabitoEntity>>` en lugar de `LiveData`** | Integración natural con Compose vía `collectAsState`, sin necesidad de `LifecycleOwner`. |
| **`rememberCoroutineScope()` en lugar de `viewModelScope`** | Las operaciones son rápidas y disparadas por la UI. Cuando el composable salga de la jerarquía, el scope se cancela automáticamente. |
| **Validación de duplicados case-insensitive** | UX más amable; "Tomar agua" y "tomar agua" son el mismo hábito. |
| **`key = { it.id }` en `items(...)`** | Permite a Compose reusar nodos y animar correctamente cuando la lista se reordena o se elimina un elemento. |
| **Textos inline en español** | No se usan `strings.xml` para el contenido; sigue el estilo de los PDFs guía. Los recursos `strings.xml` se mantienen sólo para el nombre de la app. |

---

## Notas para futuros developers

- **Cambios al schema de Room**: si modificas `HabitoEntity` (agregar/quitar columnas), debes incrementar `version` en `@Database` y proveer una migración o llamar `.fallbackToDestructiveMigration()` en el builder.
- **Inspector de Room**: Android Studio permite abrir la base `habitos_db` en vivo desde *App Inspection → Database Inspector* mientras la app corre.
- **KSP y Kotlin van apareados**: la versión de KSP (`2.2.10-2.0.2`) debe coincidir exactamente con la versión de Kotlin (`2.2.10`). Si actualizas Kotlin, actualiza KSP en el mismo commit.
- **`android.disallowKotlinSourceSets=false` en `gradle.properties`**: AGP 9 trae Kotlin "built-in" y por defecto bloquea que KSP 2.x agregue su carpeta generada al `kotlin.sourceSets`. Mientras KSP no migre al DSL `android.sourceSets`, este flag es la solución oficial recomendada y no afecta el comportamiento del compilador. Quitarlo rompe el build.
- **Plugin `kotlin-android` no aplica**: en AGP 9 + plugin `kotlin-compose`, agregar también `kotlin-android` choca con la extensión `kotlin` ya registrada. El plugin de Compose se basta para registrar Kotlin.
- **Hot reload no toca Room**: si los `@Dao_Impl` generados parecen viejos, hacer **Build → Clean Project** y resincronizar.

---

## Mejoras posibles (siguiente iteración)

Estas extensiones aparecen en los PDFs guía pero quedaron fuera del alcance "v1":

- **ViewModel + StateFlow**: separar la lógica de la UI cuando crezca.
- **Repository pattern**: si en el futuro se agregan fuentes remotas.
- **Navegación con `androidx.navigation:compose`** y pantallas adicionales (estadísticas, perfil).
- **Categorías** en `HabitoEntity` y filtrado por categoría.
- **Ordenamiento** por estado completado / fecha.
- **Tests instrumentados** del DAO con una base en memoria (`Room.inMemoryDatabaseBuilder`).
# Habitos_kt
