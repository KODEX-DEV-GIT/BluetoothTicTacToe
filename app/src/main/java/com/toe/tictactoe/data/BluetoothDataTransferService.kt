package com.toe.tictactoe.data

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.toe.tictactoe.data.mapper.toBluetoothMessage
import com.toe.tictactoe.domain.model.BluetoothMessage
import com.toe.tictactoe.domain.model.TransferFailedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.IOException

class BluetoothDataTransferService( private val socket: BluetoothSocket) {

    fun listenForIncomingMessages(): Flow<BluetoothMessage> {

        return flow {

            if(!socket.isConnected){
                return@flow
            }

            val buffer = ByteArray(1024)
            while(true) {
                val byteCount = try {
                    socket.inputStream.read(buffer)
                } catch(_: IOException) {
                    throw TransferFailedException()
                }
                val data =  buffer.decodeToString(
                    endIndex = byteCount
                )
                Log.d("message","raw message: $data")
                emit(
                  data.toBluetoothMessage(
                        isLocal = false
                    )
                )
            }

        }
    }

    suspend fun sendMessage(bytes: ByteArray): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                socket.outputStream.write(bytes)
            } catch(e: IOException) {
                e.printStackTrace()
                return@withContext false
            }

            true
        }
    }
}