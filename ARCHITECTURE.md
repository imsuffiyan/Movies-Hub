# Clean Architecture Implementation

This document outlines the Clean Architecture and MVVM implementation for the Movie App.

## Architecture Overview

The app follows Clean Architecture principles with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │  Fragment   │  │  ViewModel  │  │     UI State        │  │
│  │             │  │             │  │                     │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                    Domain Layer                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │  Use Cases  │  │ Interfaces  │  │      Models         │  │
│  │             │  │             │  │                     │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                     Data Layer                              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │ Repository  │  │   Network   │  │   Local Storage     │  │
│  │             │  │             │  │                     │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## Layer Structure

### 1. Presentation Layer
- **Fragments**: Handle UI interactions and lifecycle
- **ViewModels**: Manage UI state and business logic coordination
- **UI States**: Immutable data classes representing screen state

### 2. Domain Layer
- **Use Cases**: Encapsulate business logic operations
- **Repository Interfaces**: Define contracts for data operations
- **Models**: Core business entities

### 3. Data Layer
- **Repository Implementations**: Handle data operations
- **Network**: API communication
- **Local Storage**: SharedPreferences for favorites

## Key Components

### Use Cases
- `GetMoviesUseCase`: Handles movie fetching operations
- `FavoriteMoviesUseCase`: Manages favorite movie operations
- `SearchMoviesUseCase`: Handles movie search functionality

### ViewModels
- `HomeViewModel`: Manages home screen state and movie sections
- `CategoryListViewModel`: Handles category listing and favorites
- `MovieDetailViewModel`: Manages movie detail screen and favorites
- `SearchViewModel`: Handles search functionality with debouncing

### Repository Interfaces
- `MovieRepositoryInterface`: Defines movie data operations
- `FavoritesRepositoryInterface`: Defines favorites operations

### UI States
- `HomeUiState`: Home screen state with sections and loading states
- `CategoryUiState`: Category screen state with movies and error handling
- `SearchUiState`: Search screen state with query and results
- `MovieDetailUiState`: Detail screen state with movie info and favorite status

## Benefits of This Architecture

1. **Separation of Concerns**: Each layer has a single responsibility
2. **Testability**: Easy to unit test use cases and ViewModels
3. **Maintainability**: Changes in one layer don't affect others
4. **Scalability**: Easy to add new features following the same pattern
5. **Clean Code**: Clear dependencies and data flow

## Data Flow

1. **User Interaction** → Fragment
2. **Fragment** → ViewModel (via method calls)
3. **ViewModel** → Use Case (business logic)
4. **Use Case** → Repository Interface
5. **Repository** → Network/Local Storage
6. **Data flows back** through the same chain
7. **ViewModel** updates UI State
8. **Fragment** observes UI State and updates UI

## Dependency Injection

Using Hilt for dependency injection:
- `NetworkProviderModule`: Provides network dependencies
- `RepositoryModule`: Binds repository interfaces to implementations
- All dependencies are properly scoped and managed

## Error Handling

- Consistent error handling across all layers
- UI states include error information
- Retry mechanisms in ViewModels
- User-friendly error messages in UI

This architecture ensures the app is maintainable, testable, and follows Android development best practices.