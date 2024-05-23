package com.example.mynote.ui.viewmodel

import android.content.Context
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
            SignupViewModel(
                myNoteApplication().container.myNoteApiService
            )
        }

//        initializer {
//            HomeViewModel(
//                myNoteApplication().container.noteDao,
//                myNoteApplication().container.myNoteApiService,
//
//            )
//        }

        initializer {
            EditorViewModel(
                myNoteApplication().container.noteDao,
                myNoteApplication().container.myNoteApiService
            )
        }

        initializer {
            CategoryViewModel()
        }

        initializer {
            ProfileViewModel(
                myNoteApplication().container.noteDao,
                myNoteApplication().container.myNoteApiService
            )
        }
    }

    fun provideFactory(context: Context) = viewModelFactory {
        initializer {
            HomeViewModel(
                newMyNoteApplication(context).container.noteDao,
                newMyNoteApplication(context).container.myNoteApiService
            )
        }
    }

    private fun newMyNoteApplication(context: Context): MyNoteApplication {
        return context.applicationContext as MyNoteApplication
    }
}

fun CreationExtras.myNoteApplication(): MyNoteApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as MyNoteApplication)