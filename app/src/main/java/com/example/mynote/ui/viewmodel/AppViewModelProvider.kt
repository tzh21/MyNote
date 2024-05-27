package com.example.mynote.ui.viewmodel

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mynote.MyNoteApplication
import com.example.mynote.MyNoteViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            MyNoteViewModel(
                myNoteApplication().container.noteDao
            )
        }

        initializer {
            LoginViewModel(
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
                myNoteApplication().container.myNoteApiService,
                myNoteApplication().container.llmApiService,
            )
        }

        initializer {
            CategoryViewModel(
                myNoteApplication().container.noteDao
            )
        }

        initializer {
            ProfileViewModel(
                myNoteApplication().container.noteDao,
                myNoteApplication().container.myNoteApiService
            )
        }
    }
}

fun CreationExtras.myNoteApplication(): MyNoteApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as MyNoteApplication)