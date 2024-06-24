package com.example.facerecognitionandfirebaseapp.ui.screen.logs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.facerecognitionandfirebaseapp.data.model.AppState
import com.example.facerecognitionandfirebaseapp.data.model.FaceData
import com.example.facerecognitionandfirebaseapp.ui.theme.spacing
import com.google.firebase.Firebase
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LogsScreen(appState: AppState, host: NavHostController) {
   /* val logsChild = Firebase.database.reference.child("logs")
    val doorIsOpenRef = Firebase.database.getReference("doorIsOpen")
    val doorIsOpen = remember { mutableStateOf(false) }
    // Variável para rastrear se a porta foi aberta
    val doorWasOpened = remember { mutableStateOf(false) }

    // Listener para doorIsOpenRef
    DisposableEffect(Unit) {
        val doorIsOpenListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isOpen = snapshot.getValue(Boolean::class.java) ?: false
                doorIsOpen.value = isOpen

                // Se a porta foi aberta e agora está fechada, salva o log
                if (isOpen == false && doorWasOpened.value == true) {
                    val timestamp = System.currentTimeMillis()
                    val formattedTimestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
                    val log = FaceData(0, "Tranca Fechada", formattedTimestamp)
                    logsChild.push().setValue(log)

                    // Reinicia o estado de "porta aberta"
                    doorWasOpened.value = false
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Erro ao ler doorIsOpen: ${error.message}")
            }
        }
        doorIsOpenRef.addValueEventListener(doorIsOpenListener)
        onDispose { doorIsOpenRef.removeEventListener(doorIsOpenListener) }
    }*/

    val logsList = remember { mutableStateListOf<FaceData>() }
    val logsRef = FirebaseDatabase.getInstance().getReference("logs")

    DisposableEffect(Unit) {
        val logsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Adicionar novos logs à lista sem substituir os existentes
                for (logSnapshot in snapshot.children) {
                    val userLog = logSnapshot.getValue(FaceData::class.java)
                    if (userLog != null && !logsList.contains(userLog)) {
                        logsList.add(userLog)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Erro ao ler logs: ${error.message}")
            }
        }
        logsRef.addValueEventListener(logsListener)
        onDispose { logsRef.removeEventListener(logsListener) }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButtonPosition = FabPosition.End,
        snackbarHost = { SnackbarHost(appState.snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = contentColorFor(MaterialTheme.colorScheme.background),
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        content = { padding ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
            ) {
                Text(
                    text = "Logs",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                LazyColumn(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(MaterialTheme.spacing.Normal),
                ) {
                    items(logsList) { item ->
                        LogInfoItem(item, Modifier.padding(MaterialTheme.spacing.Small))
                    }
                   /* items(logsList) { item ->
                        if (item.name == "Tranca Aberta") {
                            doorWasOpened.value = true
                        }
                        LogInfoItem(item, Modifier.padding(MaterialTheme.spacing.Small))
                    }*/
                }
               /* LaunchedEffect(Unit) {
                    doorWasOpened.value = doorIsOpen.value
                }

                if (doorIsOpen.value == false) {
                    val timestamp = System.currentTimeMillis()
                    val formattedTimestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                        Date(timestamp)
                    )
                    val log = FaceData(0, "Tranca Fechada", formattedTimestamp)
                    logsChild.push().setValue(log)
                    // Após salvar o log, você pode redefinir doorIsOpen.value para true
                    doorIsOpen.value = true
                }*/
            }
        }
    )
}

@Composable
private fun LogInfoItem(face: FaceData, modifier: Modifier = Modifier) {
    Card(modifier) {
        Row(
            Modifier.padding(MaterialTheme.spacing.Small),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = face.name, style = MaterialTheme.typography.titleMedium)
                Text(text = face.timestamp, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
