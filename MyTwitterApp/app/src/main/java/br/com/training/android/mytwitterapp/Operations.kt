package br.com.training.android.mytwitterapp

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class Operations {

    companion object {

        fun convertStreamToString(inputStream: InputStream): String {
            val bufferReader = BufferedReader(InputStreamReader(inputStream))
            var line: String
            var allString = ""

            try {
                do {
                    line = bufferReader.readLine()

                    if (line != null) {
                        allString += line
                    }

                } while (true)

                inputStream.close()

            } catch (e: Exception) {
            }

            return allString
        }

    }

}