package com.toe.tictactoe.domain.model

import java.io.IOException

class TransferFailedException : IOException("Reading incoming data failed")