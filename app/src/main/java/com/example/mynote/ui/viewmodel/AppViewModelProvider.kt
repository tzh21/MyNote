package com.example.mynote.ui.viewmodel

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mynote.MyNoteApplication

object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for HomeViewModel
        initializer {
            LoginViewModel(
//                myNoteApplication().container.userRepository
                myNoteApplication().container.myNoteApiService
            )
        }

        initializer {
            SignupViewModel(
//                myNoteApplication().container.userRepository
                myNoteApplication().container.myNoteApiService
            )
        }

        initializer {
            HomeViewModel(
                myNoteApplication().container.noteDao,
                myNoteApplication().container.myNoteApiService
            )
        }

        initializer {
            EditorViewModel(
                myNoteApplication().container.noteDao,
                myNoteApplication().container.myNoteApiService
            )
        }

        initializer {
            CategoryViewModel()
        }
    }
}

fun CreationExtras.myNoteApplication(): MyNoteApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as MyNoteApplication)