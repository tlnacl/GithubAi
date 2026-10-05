---
name: android-di-hilt
description: |
  Hilt dependency injection setup for Android - constructor injection, @Binds and @Provides modules, component scopes, Gradle/KSP setup, Application and Android entry points, ViewModel injection, and hiltViewModel() in Compose. Use this skill whenever setting up Hilt, defining a DI module, providing a repository or ViewModel, injecting a dependency, or wiring Hilt into an Android application. Trigger on phrases like "set up Hilt", "add a Hilt module", "inject a dependency", "DI module", "hiltViewModel", "@InstallIn", "@HiltViewModel", or "@AndroidEntryPoint".
---

# Android Dependency Injection (Hilt)

## Principles

- Prefer constructor injection with `@Inject` for classes whose dependencies can be constructed directly.
- Use Hilt modules only to bind an implementation to an interface or provide values Hilt cannot construct, such as third-party types, factory results, or qualified instances.
- Put DI bindings near the implementation they provide, commonly in the owning feature or core module. Hilt aggregates modules from compile dependencies; do not manually assemble modules in `:app`.
- Install each module in the narrowest component that matches the required lifetime.
- In Compose root composables or navigation entry points, obtain ViewModels with `hiltViewModel()`. Pass state and callbacks to child composables, not the ViewModel itself.

## Constructor Injection

Annotate an injectable constructor. Hilt can then provide the class wherever its dependencies are available:

```kotlin
class OfflineFirstNotesRepository @Inject constructor(
    private val dao: NoteDao,
    private val syncManager: SyncManager,
) : NotesRepository
```

Use `@Singleton` on the implementation when the same instance should live for the application component lifetime:

```kotlin
@Singleton
class DefaultSessionManager @Inject constructor(
    private val preferences: SessionPreferences,
) : SessionManager
```

Constructor injection is also the default for use cases and ordinary dependencies. Avoid creating a module just to call an injectable constructor.

## Binding Interfaces with `@Binds`

Use an abstract module with `@Binds` when mapping an interface to an injectable implementation. The implementation must have an `@Inject` constructor:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class NotesDataModule {
    @Binds
    @Singleton
    abstract fun bindNotesRepository(
        implementation: OfflineFirstNotesRepository,
    ): NotesRepository
}
```

Use a `@Binds` method's scope to scope the binding. If the implementation itself is used through other bindings, place the scope on the injectable implementation as well where appropriate. Keep modules abstract when they contain only `@Binds` methods.

## Providing Dependencies with `@Provides`

Use an object module with `@Provides` when constructing a type Hilt cannot instantiate through an injectable constructor, or when construction requires a factory call or a qualifier:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideHttpClient(
        okHttpClient: OkHttpClient,
    ): HttpClient = HttpClientFactory.create(okHttpClient)
}
```

Provide Android framework types and third-party library types this way. Use `@ApplicationContext` for an application `Context`:

```kotlin
@Provides
@Singleton
fun provideDatabase(
    @ApplicationContext context: Context,
): NotesDatabase = Room.databaseBuilder(
    context,
    NotesDatabase::class.java,
    "notes.db",
).build()
```

Prefer separate modules for unrelated binding groups. Use qualifiers when the graph needs multiple values of the same type; apply the same qualifier at the provider and injection site.

## Components and Scopes

Choose `@InstallIn` based on who consumes the dependency and how long it should live. Common component/scope pairs:

| Component | Scope | Lifetime / typical use |
|---|---|---|
| `SingletonComponent` | `@Singleton` | Application-wide repositories, database, network clients |
| `ActivityRetainedComponent` | `@ActivityRetainedScoped` | Across activity recreation |
| `ActivityComponent` | `@ActivityScoped` | One activity instance |
| `ViewModelComponent` | `@ViewModelScoped` | One ViewModel dependency graph |
| `ServiceComponent` | `@ServiceScoped` | One service instance |

Unscoped bindings create an instance as requested within the graph. Scope only when identity or lifecycle requirements call for it. A binding's scope must be compatible with its component; do not inject a shorter-lived scoped dependency into a longer-lived one.

## Application and Android Entry Points

Annotate the `Application` subclass with `@HiltAndroidApp` to generate the application component:

```kotlin
@HiltAndroidApp
class NotesApplication : Application()
```

Declare that Application subclass in the manifest. Annotate Android framework classes that receive member injection, such as an Activity, Fragment, Service, or BroadcastReceiver, with `@AndroidEntryPoint`:

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity()
```

Use field injection only where constructor injection is not available (Android-created entry point classes). Fields must be `lateinit` or otherwise assignable and annotated with `@Inject`. Ordinary Kotlin classes should use constructor injection.

## ViewModels and Compose

Annotate a ViewModel with `@HiltViewModel` and use an `@Inject` constructor:

```kotlin
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: NotesRepository,
) : ViewModel()
```

Acquire it at the screen root or navigation entry using `hiltViewModel()`:

```kotlin
@Composable
fun NotesRoute(
    onOpenNote: (String) -> Unit,
    viewModel: NotesViewModel = hiltViewModel(),
) {
    NotesScreen(state = viewModel.state, onOpenNote = onOpenNote)
}
```

Keep screen content independently previewable/testable by passing state and event callbacks into the content composable. For navigation-scoped ViewModels, call `hiltViewModel()` within the relevant navigation entry's composition so its owner is correct.

### Assisted ViewModels

When a ViewModel needs a runtime value that is not a graph dependency, use Hilt assisted injection and an assisted factory (if supported by the project's Hilt version):

```kotlin
@HiltViewModel(assistedFactory = NoteViewModel.Factory::class)
class NoteViewModel @AssistedInject constructor(
    private val repository: NotesRepository,
    @Assisted private val noteId: String,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(noteId: String): NoteViewModel
    }
}
```

Use the `hiltViewModel<VM, VM.Factory> { factory -> factory.create(noteId) }` overload supported by the project's Compose integration. Check the installed Hilt and AndroidX Hilt versions before choosing assisted APIs; otherwise pass IDs through `SavedStateHandle` when they come from navigation arguments.

## Gradle Setup

Use the project's version catalog and convention plugins where available. Apply Hilt and its compiler to every Android/Kotlin module that declares Hilt annotations or Hilt modules. With KSP, the essential shape is:

```kotlin
plugins {
    alias(libs.plugins.hilt)
}

dependencies {
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
```

The app module must depend on feature/core modules containing Hilt bindings so Hilt can aggregate their generated metadata. Apply the Hilt application plugin to Android modules as required by the project's plugin setup. Keep processor choices consistent with project-wide KSP/KAPT configuration; do not add a second processor path unnecessarily.

Keep Hilt annotations and modules in Android modules. Configure the Hilt Gradle plugin and annotation processor consistently with the project's Android plugin and KSP/KAPT setup.

## Testing Replacements

For Hilt integration tests, use the project's Hilt test setup (`@HiltAndroidTest`, Hilt test runner/application, and `HiltAndroidRule`). Replace production bindings with `@TestInstallIn` modules or use `@UninstallModules` for narrowly scoped test replacement. Prefer repository fakes for ViewModel unit tests where a Hilt graph is not under test.

## Naming Conventions

| Thing | Convention | Example |
|---|---|---|
| Hilt module | `<Feature><Layer>Module` or descriptive `<Concern>Module` | `NotesDataModule`, `DatabaseModule` |
| Binding method | `bind<Interface>` | `bindNotesRepository` |
| Provider method | `provide<Type>` | `provideDatabase` |
| Qualifier | Descriptive qualifier annotation | `@IoDispatcher` |

## Checklist: Adding DI for a Feature

- [ ] Add `@Inject constructor` to constructible implementations and dependencies.
- [ ] Add `@Binds` for interface-to-implementation mappings and `@Provides` only for factory/third-party/framework construction.
- [ ] Install each module in the appropriate Hilt component and apply a compatible scope only when needed.
- [ ] Ensure the module's Gradle plugin/compiler setup is configured and the app depends on that module.
- [ ] Annotate the Application with `@HiltAndroidApp`; annotate Android entry points with `@AndroidEntryPoint`.
- [ ] Annotate feature ViewModels with `@HiltViewModel` and obtain them at Compose roots with `hiltViewModel()`.
- [ ] Add Hilt test replacement bindings when integration tests need a different implementation.
