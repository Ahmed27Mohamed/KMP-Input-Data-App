package com.a2004256_ahmedmohamed.inputdatadoctor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import inputdatadoctor.composeapp.generated.resources.Res
import inputdatadoctor.composeapp.generated.resources.delete
import inputdatadoctor.composeapp.generated.resources.edit
import inputdatadoctor.composeapp.generated.resources.notosansarabic
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource

class HomeScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        HomeContent(navigator)
    }
}

data class Customer2(
    val name: String,
    val phone: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(navigator: Navigator) {

    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }

    val customersList = remember {
        mutableStateListOf<Pair<String, Customer2>>()
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedCustomerId by remember { mutableStateOf<String?>(null) }

    val arabicFont = FontFamily(
        Font(Res.font.notosansarabic)
    )

    suspend fun loadCustomers() {
        customersList.clear()

        val result = FirebaseRestClient.get("users")
        if (result is JsonNull) return

        val users = result.jsonObject

        users.forEach { (key, value) ->
            val obj = value.jsonObject

            val name = obj["name"]?.jsonPrimitive?.content ?: ""
            val phone = obj["phone"]?.jsonPrimitive?.content ?: ""

            if (name.isNotBlank()) {
                customersList.add(
                    key to Customer2(
                        name = name,
                        phone = phone
                    )
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        loadCustomers()
    }

    val filteredCustomers = remember(searchQuery, customersList) {
        if (searchQuery.isBlank()) {
            customersList
        } else {
            customersList.filter { (_, customer) ->
                "${customer.name} ${customer.phone}"
                    .contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {

            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = {},
                active = false,
                onActiveChange = {},
                placeholder = {
                    Text("بحث بالاسم أو رقم الهاتف", fontFamily = arabicFont)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp)
            ) {}

            Button(
                onClick = {
                    navigator.push(AddCustomerScreen())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("إضافة مريض", fontSize = 20.sp, fontFamily = arabicFont)
            }

            Text(
                text = "إدارة المرضي",
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = arabicFont
            )

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            LazyColumn {
                items(filteredCustomers) { (id, customer) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column {
                                Text("Name: ${customer.name}", fontFamily = arabicFont)
                                Text("Phone: ${customer.phone}", fontFamily = arabicFont)
                            }

                            Row {
                                IconButton(
                                    onClick = {
                                        navigator.push(EditCustomerScreen(id))
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(Res.drawable.edit),
                                        contentDescription = "تعديل"
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        selectedCustomerId = id
                                        showDeleteDialog = true
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(Res.drawable.delete),
                                        contentDescription = "حذف"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("تأكيد الحذف", fontFamily = arabicFont) },
                text = { Text("هل تريد حذف هذا المريض؟", fontFamily = arabicFont) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            selectedCustomerId?.let { id ->
                                scope.launch {
                                    FirebaseRestClient.delete("users/$id")
                                    loadCustomers()
                                }
                            }
                            showDeleteDialog = false
                        }
                    ) {
                        Text("حذف", fontFamily = arabicFont)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("إلغاء", fontFamily = arabicFont)
                    }
                }
            )
        }
    }
}