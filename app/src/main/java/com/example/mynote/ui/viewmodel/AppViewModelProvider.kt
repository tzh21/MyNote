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
                myNoteApplication().container.userRepository
            )
        }

        initializer {
            SignupViewModel(
                myNoteApplication().container.userRepository
            )
        }

        initializer {
            HomeViewModel(
                myNoteApplication().container.noteDao
            )
        }

        initializer {
            EditorViewModel(
                myNoteApplication().container.noteDao
            )
        }

        initializer {
            CategoryViewModel()
        }
    }
}

fun CreationExtras.myNoteApplication(): MyNoteApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as MyNoteApplication)