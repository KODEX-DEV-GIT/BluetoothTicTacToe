package com.toe.tictactoe.di

import android.content.Context
import com.toe.tictactoe.data.AndroidBluetoothController
import com.toe.tictactoe.data.AndroidBluetoothServerController
import com.toe.tictactoe.domain.BluetoothController
import com.toe.tictactoe.domain.BluetoothServerController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped


@Module
@InstallIn(ViewModelComponent::class)
object AppModule {

    @Provides
    @ViewModelScoped
    fun provideBluetoothController(@ApplicationContext context: Context): BluetoothController {
        return AndroidBluetoothController(context)
    }

    @Provides
    @ViewModelScoped
    fun provideBluetoothServerController(@ApplicationContext context: Context): BluetoothServerController {
        return AndroidBluetoothServerController(context)
    }
}