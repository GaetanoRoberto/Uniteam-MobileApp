package it.polito.uniteam.gui.login


import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Text
import com.auth0.android.jwt.JWT
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.common.net.MediaType.JWT
import it.polito.uniteam.AppStateManager
import it.polito.uniteam.Factory
import it.polito.uniteam.NavControllerManager
import it.polito.uniteam.UniTeamModel
import it.polito.uniteam.gui.availability.Join
import it.polito.uniteam.gui.home.Home
import it.polito.uniteam.classes.GoogleSignInButton
import it.polito.uniteam.gui.teamDetails.TeamDetailsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.security.SecureRandom

class LoginViewModel(val model: UniTeamModel, val savedStateHandle: SavedStateHandle): ViewModel() {
    val isUserLogged = model.isUserLogged
    //val loggedMember = model.loggedUser
    val setLoggedMember = model::setLoggedUser
    val setIsUserLogged = model::setIsUserLogged
}

@Preview
@Composable
fun Login(vm: LoginViewModel = viewModel(factory = Factory(LocalContext.current.applicationContext)), joinTeam : Boolean= false) {
    val WEB_CLIENT_ID = "978760585545-12q57615j1peajc0o0u1dufbcb97bjn7.apps.googleusercontent.com"
    val activityContext = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var logged by remember {
        vm.isUserLogged
    }
    var userInfo by remember { mutableStateOf("") }
    var userInfoName by remember { mutableStateOf("") }
    var userInfoEmail by remember { mutableStateOf("") }


    val googleIdOption : GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(WEB_CLIENT_ID)
        .setAutoSelectEnabled(true)
        .setNonce(generateNonce())
        .build()

    val request : GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    suspend fun handleSignIn(result : GetCredentialResponse) {
        val credential = result.credential

        when (credential) {
            is PublicKeyCredential -> {
                val responseJson = credential.authenticationResponseJson
                Log.d("LOGIN", "PublicKeyCredential: $responseJson")
            }

            is PasswordCredential -> {
                val username = credential.id
                val password = credential.password
                Log.d("LOGIN", "PasswordCredential: $username, $password")
                userInfo = "Username: $username"
                logged = true
            }

            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        val jwt = JWT(idToken)
                        val email = jwt.getClaim("email").asString()
                        val name = jwt.getClaim("name").asString()
                        userInfo = "Name: $name, Email: $email"
                        userInfoName = name!!
                        userInfoEmail = email!!

                        vm.setLoggedMember(jwt)
                        vm.setIsUserLogged(true)
                        Log.d("LOGIN", "GoogleIdTokenCredential: $userInfo")

                    } catch (e : Exception) {
                        Log.e("LOGIN", "Error parsing ID token", e)
                    }
                }
                else {
                    Log.e("LOGIN", "Unexpected type of credential")
                }
            }
            else -> {
                Log.e("LOGIN", "Unexpected type of credential")
            }
        }

    }

    suspend fun startLogin() {
        try {
            val credentialManager = CredentialManager.create(activityContext)
            val result = credentialManager.getCredential(
                request = request,
                context = activityContext,
            )
            handleSignIn(result)
        } catch (e : GetCredentialException) {
            Log.e("LOGIN", "GetCredentialException: ${e.message}", e)
        }
    }

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .build()
    val googleSignInClient = GoogleSignIn.getClient(activityContext, gso)


    fun logoutGoogle() {
        googleSignInClient.signOut().addOnCompleteListener {
            vm.setIsUserLogged(false)
            //after log out
        }
    }


   /* LaunchedEffect(key1 = 1) {
        coroutineScope.launch {
            startLogin()
        }

    }*/


    if (vm.isUserLogged.value && joinTeam) {
        Log.d("LOGIN", vm.isUserLogged.value.toString() )
        Join()
    }
    else {
        Log.d("LOGIN", vm.isUserLogged.value.toString() )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(1f), horizontalArrangement = Arrangement.Center) {
                GoogleSignInButton(onClick = {
                    if (!vm.isUserLogged.value) {
                        coroutineScope.launch {
                            startLogin()
                        }
                    } else {
                        logoutGoogle()
                    }

                })
                /*FilledTonalButton(
                    modifier = Modifier
                        .width(120.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), onClick = {
                        if (!vm.isUserLogged.value) {
                            coroutineScope.launch {
                                startLogin()
                            }
                        }
                    }) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!vm.isUserLogged.value) {
                            Text("Login", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Logout", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }*/
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(1f), horizontalArrangement = Arrangement.Center) {
                if (logged) {
                    Log.d("LOGIN", "LOGGED")
                    if(vm.isUserLogged.value)
                        NavControllerManager.getNavController().navigate("Teams")
                    //Text("Hello: ${userInfoName}")

                } else {
                    Log.d("LOGIN", "NOT LOGGED")

                    Text("Please log in to continue...")
                }
            }


        }


    }

}

fun generateNonce(): String {
    val nonceBytes = ByteArray(16)
    SecureRandom().nextBytes(nonceBytes)
    return Base64.encodeToString(nonceBytes, Base64.NO_WRAP)
}