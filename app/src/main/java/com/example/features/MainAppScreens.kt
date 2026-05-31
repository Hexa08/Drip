package com.example.features

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.lazy.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.core.models.Category
import com.example.core.models.Product
import com.example.core.network.RetrofitInstance
import com.example.core.services.AddressItem
import com.example.core.services.CartItem
import com.example.core.services.FirebaseService
import com.example.core.services.OrderDoc
import com.example.shared.widgets.*
import com.example.ui.theme.GoldPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --------------------------------------------------------------------------
// 1. SPLASH SCREEN
// --------------------------------------------------------------------------
@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var startAnimation by remember { mutableStateOf(false) }
    val bounceScale by animateFloatAsState(
        targetValue = if (startAnimation) 1.2f else 0.8f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 120f),
        label = "SplashBounce"
    )

    // Animated gold luxury shimmer brush
    val infiniteTransition = rememberInfiniteTransition(label = "Shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SplashShimmer"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(GoldPrimary, Color(0xFFFFF1D6), GoldPrimary),
        start = androidx.compose.ui.geometry.Offset(shimmerOffset, 0f),
        end = androidx.compose.ui.geometry.Offset(shimmerOffset + 150f, 150f)
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2200) // Aesthetic duration

        // Check onboarding state from SharedPreferences
        val prefs = context.getSharedPreferences("drip_prefs", Context.MODE_PRIVATE)
        val onboardingDone = prefs.getBoolean("onboarding_done", false)
        val currentUser = FirebaseService.currentUserState.value

        if (!onboardingDone) {
            navController.navigate("onboarding") {
                popUpTo("splash") { inclusive = true }
            }
        } else if (currentUser == null) {
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            navController.navigate("navbar") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = bounceScale
                        scaleY = bounceScale
                    }
                    .size(120.dp)
                    .background(Color(0xFF1E1E1E), shape = RoundedCornerShape(32.dp))
                    .padding(3.dp)
                    .background(Color(0xFF0D0D0D), shape = RoundedCornerShape(30.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Shimmered brand name
            Text(
                text = "DRIP",
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 8.sp,
                style = TextStyle(brush = shimmerBrush)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "FASHION STORE",
                fontSize = 12.sp,
                color = Color(0xFF9E9E9E),
                letterSpacing = 4.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// --------------------------------------------------------------------------
// 2. ONBOARDING SCREEN
// --------------------------------------------------------------------------
data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val bgGradient: List<Color>
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(navController: NavController) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    val pages = listOf(
        OnboardingPage(
            "Discover Premium Fashion",
            "Browse curated boutique jackets, kicks, hoodies, accessories, and luxury streetwear tailored for perfect expressivity.",
            Icons.Default.Dashboard,
            listOf(Color(0xFF1E170E), Color(0xFF0D0D0D))
        ),
        OnboardingPage(
            "Shop the Latest Trends",
            "Real-time integrations with our designer closets. Tap your wishlist instantly and check out on lightning schedules.",
            Icons.Default.TrendingUp,
            listOf(Color(0xFF0E1A17), Color(0xFF0D0D0D))
        ),
        OnboardingPage(
            "Fast Delivery, Easy Returns",
            "Complimentary shipping on orders above $100. Smooth home delivery with transparent trackable routes.",
            Icons.Default.LocalShipping,
            listOf(Color(0xFF13131F), Color(0xFF0D0D0D))
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val page = pages[pageIndex]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(page.bgGradient))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = page.icon,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(54.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(44.dp))

                    Text(
                        text = page.title,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 36.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = page.description,
                        fontSize = 15.sp,
                        color = Color(0xFF9E9E9E),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }

        // Top Bar standard: SKIP button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Skip",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .bounceClick {
                        val prefs = context.getSharedPreferences("drip_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putBoolean("onboarding_done", true).apply()
                        navController.navigate("login") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                    .padding(8.dp)
            )
        }

        // Bottom styling indicator / GET STARTED button
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Smooth page indicator ticks
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 0..2) {
                    val active = pagerState.currentPage == i
                    val width by animateDpAsState(if (active) 24.dp else 8.dp, label = "IndicatorWidth")
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(if (active) GoldPrimary else Color.White.copy(alpha = 0.2f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Button
            DripButton(
                text = if (pagerState.currentPage == 2) "Get Started" else "Next",
                onClick = {
                    if (pagerState.currentPage < 2) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        val prefs = context.getSharedPreferences("drip_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putBoolean("onboarding_done", true).apply()
                        navController.navigate("login") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}

// --------------------------------------------------------------------------
// 3. AUTH LAYOUTS
// --------------------------------------------------------------------------
@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DRIP",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 3.sp
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Welcome Back",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Sign in to access premium streetwear closets",
                fontSize = 14.sp,
                color = Color(0xFF9E9E9E)
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address", color = Color(0xFF9E9E9E)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = Color(0xFF2C2C2C),
                    focusedLabelColor = GoldPrimary,
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = Color(0xFF9E9E9E)) },
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = Color(0xFF2C2C2C),
                    focusedLabelColor = GoldPrimary,
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Forgot Password?",
                    color = GoldPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.bounceClick {
                        navController.navigate("forgot_password")
                    }
                )
            }

            if (isError) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage,
                    color = Color(0xFFFF4D4D),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            DripButton(
                text = "Sign In",
                onClick = {
                    if (email.isEmpty() || password.isEmpty()) {
                        isError = true
                        errorMessage = "Please enter email and password."
                    } else if (!email.contains("@")) {
                        isError = true
                        errorMessage = "Invalid email format."
                    } else {
                        isError = false
                        FirebaseService.login(email, password, {
                            navController.navigate("navbar") {
                                popUpTo("login") { inclusive = true }
                            }
                        }, { err ->
                            isError = true
                            errorMessage = err
                        })
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Google Sign In fallback simulation
            Box(
                modifier = Modifier
                    .bounceClick {
                        FirebaseService.login("google.user@gmail.com", "google-oauth", {
                            navController.navigate("navbar") {
                                popUpTo("login") { inclusive = true }
                            }
                        }, {})
                    }
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, Color(0xFF2C2C2C), RoundedCornerShape(14.dp))
                    .background(Color(0xFF141414)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(GoldPrimary, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "G",
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Continue with Google",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // SignUp Navigation link footer
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Don't have an account? ",
                color = Color(0xFF9E9E9E),
                fontSize = 14.sp
            )
            Text(
                text = "Sign Up",
                color = GoldPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.bounceClick {
                    navController.navigate("signup")
                }
            )
        }
    }
}

@Composable
fun SignUpScreen(navController: NavController) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorState by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        ) {
            Text(
                text = "Join DRIP",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = "Create an account to track orders and save wishlists",
                fontSize = 14.sp,
                color = Color(0xFF9E9E9E)
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name", color = Color(0xFF9E9E9E)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = Color(0xFF2C2C2C),
                    focusedLabelColor = GoldPrimary,
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address", color = Color(0xFF9E9E9E)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = Color(0xFF2C2C2C),
                    focusedLabelColor = GoldPrimary,
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number", color = Color(0xFF9E9E9E)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = Color(0xFF2C2C2C),
                    focusedLabelColor = GoldPrimary,
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Create Password", color = Color(0xFF9E9E9E)) },
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = Color(0xFF2C2C2C),
                    focusedLabelColor = GoldPrimary,
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (errorState.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = errorState, color = Color(0xFFFF4D4D), fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            DripButton(
                text = "Sign Up",
                onClick = {
                    if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                        errorState = "Please complete all required fields."
                    } else if (password.length < 6) {
                        errorState = "Password must be at least 6 characters."
                    } else {
                        FirebaseService.signUp(name, email, phone, {
                            navController.navigate("navbar") {
                                popUpTo("signup") { inclusive = true }
                            }
                        }, { err ->
                            errorState = err
                        })
                    }
                }
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Already have an account? ",
                color = Color(0xFF9E9E9E),
                fontSize = 14.sp
            )
            Text(
                text = "Sign In",
                color = GoldPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.bounceClick {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun ForgotPasswordScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        ) {
            Icon(
                imageVector = Icons.Default.LockReset,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Reset Password",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = "Enter your email address to receive password recovery details.",
                fontSize = 14.sp,
                color = Color(0xFF9E9E9E)
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (!isSubmitted) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address", color = Color(0xFF9E9E9E)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = Color(0xFF2C2C2C),
                        focusedLabelColor = GoldPrimary,
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(32.dp))

                DripButton(
                    text = "Transmit Reset Instructions",
                    onClick = {
                        if (email.isNotEmpty() && email.contains("@")) {
                            FirebaseService.resetPassword(email, {
                                isSubmitted = true
                            }, {})
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GoldPrimary.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Success! Password recovery instructions have been successfully delivered to: $email. Check your inbox.",
                        color = GoldPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                DripButton(
                    text = "Return to Sign In",
                    onClick = { navController.popBackStack() }
                )
            }
        }
    }
}

// --------------------------------------------------------------------------
// 4. NAVBAR CONTAINER & HOME SCREEN
// --------------------------------------------------------------------------
@Composable
fun NavbarContainer(navController: NavController, darkTheme: Boolean, onThemeToggle: (Boolean) -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                val tabs = listOf(
                    Triple(Icons.Default.Home, Icons.Outlined.Home, "Home"),
                    Triple(Icons.Default.Search, Icons.Outlined.Search, "Explore"),
                    Triple(Icons.Default.Favorite, Icons.Outlined.FavoriteBorder, "Wishlist"),
                    Triple(Icons.Default.ShoppingCart, Icons.Outlined.ShoppingCart, "Cart"),
                    Triple(Icons.Default.Person, Icons.Outlined.Person, "Profile")
                )

                val cartItems by FirebaseService.cartStream.collectAsState()
                val totalCartCount = cartItems.sumOf { it.quantity }

                tabs.forEachIndexed { index, (filled, outline, label) ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            Box {
                                Icon(
                                    imageVector = if (isSelected) filled else outline,
                                    contentDescription = label,
                                    tint = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                // Render Badge Count on Cart tab
                                if (label == "Cart" && totalCartCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 10.dp, y = (-7).dp)
                                            .background(GoldPrimary, CircleShape)
                                            .size(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = totalCartCount.toString(),
                                            color = Color.Black,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = GoldPrimary.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> HomeScreen(navController)
                1 -> ExploreScreen(navController)
                2 -> WishlistScreen(navController)
                3 -> CartScreen(navController)
                4 -> ProfileScreen(navController, darkTheme, onThemeToggle)
            }
        }
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    val currentUser by FirebaseService.currentUserState.collectAsState()
    val cartItems by FirebaseService.cartStream.collectAsState()
    val totalCartCount = cartItems.sumOf { it.quantity }

    var selectedCategoryId by remember { mutableStateOf(0) }
    var isOffline by remember { mutableStateOf(false) }

    // Loading & Items
    var isLoadingCategories by remember { mutableStateOf(true) }
    var isLoadingProducts by remember { mutableStateOf(true) }
    var categoriesList by remember { mutableStateOf<List<Category>>(emptyList()) }
    var productsList by remember { mutableStateOf<List<Product>>(emptyList()) }

    // Retrofit Call with offline resilience
    LaunchedEffect(Unit) {
        try {
            isLoadingCategories = true
            val fetchedCats = RetrofitInstance.api.getCategories()
            categoriesList = listOf(Category(0, "All Drip", "")) + fetchedCats.take(6)
            isLoadingCategories = false
        } catch (e: Exception) {
            categoriesList = listOf(Category(0, "All Drip", "")) + RetrofitInstance.fallbackCategories
            isLoadingCategories = false
            isOffline = true
        }

        try {
            isLoadingProducts = true
            val fetchedProds = RetrofitInstance.api.getProducts(limit = 20)
            productsList = fetchedProds
            isLoadingProducts = false
        } catch (e: Exception) {
            productsList = RetrofitInstance.fallbackProducts
            isLoadingProducts = false
            isOffline = true
        }
    }

    // Countdown Timer widget
    var countdownText by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        var baseSeconds = 4 * 60 * 60L // 4 hours
        while (baseSeconds > 0) {
            val h = baseSeconds / 3600
            val m = (baseSeconds % 3600) / 60
            val s = baseSeconds % 60
            countdownText = String.format("%02d:%02d:%02d", h, m, s)
            delay(1000L)
            baseSeconds--
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Header Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Hello, ${currentUser?.name ?: "Drip Guest"} 👋",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Curate your look beautifully today",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Cart Icon on top with active badge count
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        .padding(5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        OfflineBanner(isOffline = isOffline)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Category list horizontal chips
            item {
                if (isLoadingCategories) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        repeat(4) { ShimmerLoader(Modifier.size(80.dp, 36.dp)) }
                    }
                } else {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(categoriesList) { cat ->
                            CategoryChip(
                                name = cat.name,
                                isSelected = selectedCategoryId == cat.id,
                                onSelected = { selectedCategoryId = cat.id }
                            )
                        }
                    }
                }
            }

            // Promotional Carousel Hero banner (3 hardcoded design cards with horizontal rolling)
            item {
                PromotionalHeroCarousel(navController, productsList)
            }

            // Flash Sale Section with Countdown
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Flash Sale",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE25C5C), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = countdownText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Text(
                        text = "See All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                }
            }

            // Flash Sale horizontal items (tailored 4 items)
            item {
                if (isLoadingProducts) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        repeat(2) { ShimmerLoader(Modifier.size(160.dp, 290.dp)) }
                    }
                } else {
                    val filteredProds = if (selectedCategoryId == 0) {
                        productsList
                    } else {
                        productsList.filter { it.category.id == selectedCategoryId }
                    }

                    if (filteredProds.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp), contentAlignment = Alignment.Center
                        ) {
                            Text("No category matching available")
                        }
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredProds.takeLast(4)) { prod ->
                                ProductCard(
                                    product = prod,
                                    onNavigateDetail = {
                                        navController.navigate("product_detail/${prod.id}")
                                    },
                                    modifier = Modifier.width(165.dp)
                                )
                            }
                        }
                    }
                }
            }

            // New Arrivals Scroll Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New Arrivals ✨",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            item {
                if (isLoadingProducts) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        repeat(2) { ShimmerLoader(Modifier.size(160.dp, 290.dp)) }
                    }
                } else {
                    val filteredProds = if (selectedCategoryId == 0) {
                        productsList
                    } else {
                        productsList.filter { it.category.id == selectedCategoryId }
                    }
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredProds.take(8)) { prod ->
                            ProductCard(
                                product = prod,
                                onNavigateDetail = {
                                    navController.navigate("product_detail/${prod.id}")
                                },
                                modifier = Modifier.width(165.dp)
                            )
                        }
                    }
                }
            }

            // Trending Now Section (2-Column Grid Representation inside LazyColumn)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trending Now 🔥",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            item {
                if (isLoadingProducts) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ShimmerLoader(Modifier.weight(1f).height(290.dp))
                            ShimmerLoader(Modifier.weight(1f).height(290.dp))
                        }
                    }
                } else {
                    val filteredProds = if (selectedCategoryId == 0) {
                        productsList
                    } else {
                        productsList.filter { it.category.id == selectedCategoryId }
                    }
                    val trending = filteredProds.drop(8).take(8)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        for (i in trending.indices step 2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                val item1 = trending[i]
                                ProductCard(
                                    product = item1,
                                    onNavigateDetail = {
                                        navController.navigate("product_detail/${item1.id}")
                                    },
                                    modifier = Modifier.weight(1f)
                                )

                                if (i + 1 < trending.size) {
                                    val item2 = trending[i + 1]
                                    ProductCard(
                                        product = item2,
                                        onNavigateDetail = {
                                            navController.navigate("product_detail/${item2.id}")
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PromotionalHeroCarousel(navController: NavController, products: List<Product>) {
    var promoIndex by remember { mutableStateOf(0) }
    val promotions = listOf(
        Triple("SUMMER CLOSINGS", "GET UP TO 50% OFF", Color(0xFF1E1405)),
        Triple("LUXURY LEATHER", "PREMIUM CRAFT ACCESSORIES", Color(0xFF0F121C)),
        Triple("STREET ESSENTIALS", "GOLD DETAIL LUXE SNEAKERS", Color(0xFF141F16))
    )

    // Slides scrollable effect
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(promotions[promoIndex].third)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = { /* handle left/right swap quickly */ },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        if (dragAmount.x < -20) {
                            promoIndex = (promoIndex + 1) % promotions.size
                        } else if (dragAmount.x > 20) {
                            promoIndex = if (promoIndex == 0) promotions.size - 1 else promoIndex - 1
                        }
                    }
                )
            }
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(220.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .background(GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = promotions[promoIndex].first,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldPrimary,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = promotions[promoIndex].second,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Swipe to view collections",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold
            )
        }

        // Gold hanger detail decoration overlay
        Icon(
            imageVector = Icons.Default.WaterDrop,
            contentDescription = null,
            tint = GoldPrimary.copy(alpha = 0.15f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(110.dp)
                .offset(x = 20.dp, y = 20.dp)
        )

        // Indicators
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(promotions.size) { i ->
                val active = promoIndex == i
                Box(
                    modifier = Modifier
                        .size(if (active) 12.dp else 6.dp, 6.dp)
                        .clip(CircleShape)
                        .background(if (active) GoldPrimary else Color.White.copy(alpha = 0.3f))
                )
            }
        }
    }
}

// --------------------------------------------------------------------------
// 5. EXPLORE SCREEN
// --------------------------------------------------------------------------
@Composable
fun ExploreScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var selectedCatId by remember { mutableStateOf(0) }
    var displayProducts by remember { mutableStateOf<List<Product>>(emptyList()) }

    var isSortingOpen by remember { mutableStateOf(false) }
    var isFilterOpen by remember { mutableStateOf(false) }

    var selectedSortBy by remember { mutableStateOf("Popular") }
    var minPriceSelector by remember { mutableStateOf(0f) }
    var maxPriceSelector by remember { mutableStateOf(300f) }

    LaunchedEffect(Unit) {
        try {
            val cats = RetrofitInstance.api.getCategories()
            categories = listOf(Category(0, "All Drip", "")) + cats.take(6)
        } catch (e: Exception) {
            categories = listOf(Category(0, "All Drip", "")) + RetrofitInstance.fallbackCategories
        }

        try {
            val prods = RetrofitInstance.api.getProducts(limit = 40)
            products = prods
            displayProducts = prods
        } catch (e: Exception) {
            products = RetrofitInstance.fallbackProducts
            displayProducts = RetrofitInstance.fallbackProducts
        }
    }

    // Filter computation
    val filteredList = products.filter { prod ->
        val matchesQuery = prod.title.contains(searchQuery, ignoreCase = true)
        val matchesCat = selectedCatId == 0 || prod.category.id == selectedCatId
        val matchesPrice = prod.price >= minPriceSelector && prod.price <= maxPriceSelector
        matchesQuery && matchesCat && matchesPrice
    }.let { unsorted ->
        when (selectedSortBy) {
            "Price Low-High" -> unsorted.sortedBy { it.price }
            "Price High-Low" -> unsorted.sortedByDescending { it.price }
            "Newest" -> unsorted.sortedByDescending { it.id }
            else -> unsorted // Popular (default index sorting)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Filter header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search elegant clothes...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = GoldPrimary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            // Dynamic filter action button
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .bounceClick { isFilterOpen = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.FilterList, "Filter Options", tint = GoldPrimary)
            }
        }

        // Horizontal selections
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(categories) { cat ->
                CategoryChip(
                    name = cat.name,
                    isSelected = selectedCatId == cat.id,
                    onSelected = { selectedCatId = cat.id }
                )
            }
        }

        // Quick Sorting indicators selection row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredList.size} items found",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.bounceClick { isSortingOpen = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sort: $selectedSortBy",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )
                Icon(Icons.Default.ArrowDropDown, null, tint = GoldPrimary)
            }
        }

        if (filteredList.isEmpty()) {
            EmptyState(
                title = "No products found",
                description = "We couldn't locate any matching apparel in the closet. Clear filters and search again.",
                icon = Icons.Default.Inventory
            )
        } else {
            // Grid Display
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList) { prod ->
                    ProductCard(
                        product = prod,
                        onNavigateDetail = {
                            navController.navigate("product_detail/${prod.id}")
                        }
                    )
                }
            }
        }
    }

    // Sort Bottom Sheet simulation
    if (isSortingOpen) {
        AlertDialog(
            onDismissRequest = { isSortingOpen = false },
            confirmButton = {},
            title = { Text("Sort By", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                val sortOptions = listOf("Popular", "Newest", "Price Low-High", "Price High-Low")
                Column {
                    sortOptions.forEach { opt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedSortBy = opt
                                    isSortingOpen = false
                                }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = opt, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
                            if (selectedSortBy == opt) {
                                Icon(Icons.Default.Check, null, tint = GoldPrimary)
                            }
                        }
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Filter range Bottom Sheet simulation
    if (isFilterOpen) {
        AlertDialog(
            onDismissRequest = { isFilterOpen = false },
            title = { Text("Filters", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Price Range",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    RangeSlider(
                        value = minPriceSelector..maxPriceSelector,
                        onValueChange = { range ->
                            minPriceSelector = range.start
                            maxPriceSelector = range.endInclusive
                        },
                        valueRange = 0f..300f,
                        colors = SliderDefaults.colors(
                            thumbColor = GoldPrimary,
                            activeTrackColor = GoldPrimary
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("$${minPriceSelector.toInt()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$${maxPriceSelector.toInt()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { isFilterOpen = false }) {
                    Text("Apply", color = GoldPrimary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// --------------------------------------------------------------------------
// 6. PRODUCT DETAIL SCREEN
// --------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(navController: NavController, productId: Int) {
    val context = LocalContext.current
    var product by remember { mutableStateOf<Product?>(null) }
    var isOffline by remember { mutableStateOf(false) }

    var selectedSize by remember { mutableStateOf("M") }
    var selectedColorName by remember { mutableStateOf("Crimson") }
    var quantity by remember { mutableStateOf(1) }

    var selectedImageIndex by remember { mutableStateOf(0) }
    var isDescExpanded by remember { mutableStateOf(false) }

    // Flying cart animation parameters
    val coroutineScope = rememberCoroutineScope()
    var showFlyingToast by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        try {
            product = RetrofitInstance.api.getProduct(productId)
        } catch (e: Exception) {
            product = RetrofitInstance.fallbackProducts.find { it.id == productId } ?: RetrofitInstance.fallbackProducts.first()
            isOffline = true
        }
    }

    if (product == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = GoldPrimary)
        }
        return
    }

    val prod = product!!

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { FirebaseService.toggleWishlist(prod) }) {
                        val wishlist by FirebaseService.wishlistStream.collectAsState()
                        val isFav = wishlist.any { it.productId == prod.id.toString() }
                        Icon(
                            imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Toggle Wishlist",
                            tint = if (isFav) GoldPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padValues ->
        Box(modifier = Modifier.fillMaxSize().padding(padValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Large Image gallery with thumb indicators
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    val currentImageUrl = prod.images.getOrNull(selectedImageIndex) ?: ""
                    AsyncImage(
                        model = currentImageUrl,
                        contentDescription = prod.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Thumbnails list Overlay at bottom center
                    if (prod.images.size > 1) {
                        LazyRow(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp)
                                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(prod.images) { idx, url ->
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            2.dp,
                                            if (selectedImageIndex == idx) GoldPrimary else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedImageIndex = idx }
                                ) {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Category & Stars Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = prod.category.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                        }

                        // Rating info
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StarRating(rating = prod.rating, size = 13f)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${prod.reviewCount} Reviews)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Title
                    Text(
                        text = prod.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Pricing with 20% OFF representation
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "$${String.format("%.2f", prod.discountedPrice)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldPrimary
                        )

                        Text(
                            text = "$${String.format("%.2f", prod.price)}",
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = LocalTextStyle.current.copy(textDecoration = TextDecoration.LineThrough)
                        )

                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE25C5C), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "SAVE 20%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Expandable Description
                    Text(
                        text = "Description",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = prod.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp,
                        maxLines = if (isDescExpanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .clickable { isDescExpanded = !isDescExpanded }
                            .animateContentSize()
                    )
                    Text(
                        text = if (isDescExpanded) "Read Less" else "Read More",
                        color = GoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { isDescExpanded = !isDescExpanded }
                            .padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Color Selector
                    Text(
                        text = "Selected Color: $selectedColorName",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    val colorsList = listOf(
                        Pair("Crimson", Color(0xFF9E1F1F)),
                        Pair("Navy", Color(0xFF1F2F5E)),
                        Pair("Beige", Color(0xFFDCD2C3)),
                        Pair("Olive", Color(0xFF535E3F))
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        colorsList.forEach { (name, colHex) ->
                            val isSelected = selectedColorName == name
                            Box(
                                modifier = Modifier
                                    .bounceClick { selectedColorName = name }
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colHex)
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = GoldPrimary,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Size Selector
                    Text(
                        text = "Select Size",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    val sizes = listOf("XS", "S", "M", "L", "XL")
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        sizes.forEach { sz ->
                            val active = selectedSize == sz
                            Box(
                                modifier = Modifier
                                    .bounceClick { selectedSize = sz }
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (active) GoldPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(1.dp)
                                    .background(if (active) GoldPrimary else MaterialTheme.colorScheme.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sz,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) Color.Black else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Quantity Stepper
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Quantity",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .bounceClick { if (quantity > 1) quantity-- }
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Remove, "Reduce", modifier = Modifier.size(16.dp))
                            }

                            Text(
                                text = quantity.toString(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )

                            Box(
                                modifier = Modifier
                                    .bounceClick { quantity++ }
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, "Increase", modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    // Add to Cart
                    DripButton(
                        text = "Add to Cart  —  $${String.format("%.2f", prod.discountedPrice * quantity)}",
                        onClick = {
                            FirebaseService.addToCart(prod, selectedSize, selectedColorName, quantity)
                            showFlyingToast = true
                            coroutineScope.launch {
                                delay(2000L)
                                showFlyingToast = false
                            }
                        }
                    )
                }
            }

            // Flying Notification Banner overlay
            AnimatedVisibility(
                visible = showFlyingToast,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).padding(16.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Added to Cart!",
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "View",
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier
                                .clickable {
                                    showFlyingToast = false
                                }
                        )
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------------------
// 7. CART SCREEN
// --------------------------------------------------------------------------
@Composable
fun CartScreen(navController: NavController) {
    val context = LocalContext.current
    val cartItems by FirebaseService.cartStream.collectAsState()

    var promoCode by remember { mutableStateOf("") }
    var lastAppliedDiscount by remember { mutableStateOf(0.0) } // ratio discount e.g. 10% = 0.1
    var promoError by remember { mutableStateOf<String?>(null) }
    var promoSuccess by remember { mutableStateOf<String?>(null) }

    val subtotal = cartItems.sumOf { it.price * it.quantity }
    val shipping = if (subtotal > 100.0) 0.0 else if (subtotal == 0.0) 0.0 else 9.99
    val discountAmount = subtotal * lastAppliedDiscount
    val total = subtotal + shipping - discountAmount

    if (cartItems.isEmpty()) {
        EmptyState(
            title = "Your Cart is Empty",
            description = "Drip your luxury collection! Add jackets, footwear, and boutique garments to your bag.",
            icon = Icons.Default.ShoppingCart
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Shopping Cart Bag",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Items list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(cartItems) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "Size: ${item.selectedSize}  |  Color: ${item.selectedColor}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$${String.format("%.2f", item.price)}",
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )

                            // Stepper
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .bounceClick {
                                            FirebaseService.updateCartQuantity(
                                                item.productId,
                                                item.selectedSize,
                                                item.selectedColor,
                                                item.quantity - 1
                                            )
                                        }
                                        .size(26.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Remove, null, modifier = Modifier.size(12.dp))
                                }

                                Text(
                                    text = item.quantity.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )

                                Box(
                                    modifier = Modifier
                                        .bounceClick {
                                            FirebaseService.updateCartQuantity(
                                                item.productId,
                                                item.selectedSize,
                                                item.selectedColor,
                                                item.quantity + 1
                                            )
                                        }
                                        .size(26.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Add, null, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }

                    // Delete Trash Icon Button
                    IconButton(
                        onClick = {
                            FirebaseService.deleteFromCart(item.productId, item.selectedSize, item.selectedColor)
                        },
                        modifier = Modifier.align(Alignment.Top)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove",
                            tint = Color(0xFFFF4D4D)
                        )
                    }
                }
            }
        }

        // Summary details with promo codes
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Promo Input field
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = promoCode,
                    onValueChange = { promoCode = it },
                    placeholder = { Text("Promo Code (e.g. DRIP20)", fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                Box(
                    modifier = Modifier
                        .height(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(GoldPrimary)
                        .bounceClick {
                            val upper = promoCode.trim().uppercase()
                            if (upper == "DRIP10") {
                                lastAppliedDiscount = 0.10
                                promoSuccess = "10% Discount applied!"
                                promoError = null
                            } else if (upper == "DRIP20") {
                                lastAppliedDiscount = 0.20
                                promoSuccess = "20% Discount applied!"
                                promoError = null
                            } else {
                                promoError = "Invalid promo code."
                                promoSuccess = null
                            }
                        }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Apply", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            if (promoError != null) {
                Text(text = promoError!!, color = Color(0xFFFF4D4D), fontSize = 11.sp)
            }
            if (promoSuccess != null) {
                Text(text = promoSuccess!!, color = Color(0xFF4CAF50), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Subtotal", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                Text("$${String.format("%.2f", subtotal)}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Shipping Fee", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                Text(
                    text = if (shipping == 0.0) "FREE" else "$${String.format("%.2f", shipping)}",
                    fontWeight = FontWeight.Bold,
                    color = if (shipping == 0.0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )
            }

            if (discountAmount > 0.0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Promo Discount", color = Color(0xFF4CAF50), fontSize = 14.sp)
                    Text("-$${String.format("%.2f", discountAmount)}", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50), fontSize = 14.sp)
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Payment", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("$${String.format("%.2f", total)}", fontWeight = FontWeight.ExtraBold, color = GoldPrimary, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Proceed
            DripButton(
                text = "Proceed to Checkout",
                onClick = {
                    navController.navigate("checkout/$total")
                }
            )
        }
    }
}

// --------------------------------------------------------------------------
// 8. CHECKOUT SCREEN
// --------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(navController: NavController, totalAmount: Double) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(1) } // 1: Address, 2: Payment, 3: Review

    val addressItems by FirebaseService.addressStream.collectAsState()
    var selectedAddressIndex by remember { mutableStateOf(0) }

    // Card state
    var cardName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }
    var cardFlipped by remember { mutableStateOf(false) }

    // Selected payment method
    var paymentMethod by remember { mutableStateOf("Credit Card") } // Credit Card, COD, bKash

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout Wizard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padValues)
                .padding(16.dp)
        ) {
            // Top Step Progress Indicator
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepIndicator(step = 1, currentStep = currentStep, label = "Address")
                Divider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = MaterialTheme.colorScheme.outline)
                StepIndicator(step = 2, currentStep = currentStep, label = "Payment")
                Divider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = MaterialTheme.colorScheme.outline)
                StepIndicator(step = 3, currentStep = currentStep, label = "Review")
            }

            Box(modifier = Modifier.weight(1f)) {
                when (currentStep) {
                    1 -> {
                        // STEP 1: ADDRESS
                        Column {
                            Text("Select Delivery Address", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(14.dp))
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                itemsIndexed(addressItems) { idx, addr ->
                                    val isSelected = selectedAddressIndex == idx
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                                            .clickable { selectedAddressIndex = idx }
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = addr.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(text = addr.address, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(text = "${addr.city}, ${addr.postalCode}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(text = addr.phone, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { selectedAddressIndex = idx },
                                            colors = RadioButtonDefaults.colors(selectedColor = GoldPrimary)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            DripButton(text = "Go to Payment", onClick = { currentStep = 2 })
                        }
                    }

                    2 -> {
                        // STEP 2: PAYMENT WITH CARD FLIP ANIMATION
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            Text("Payment Method", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Payment method selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Credit Card", "COD", "bKash").forEach { payWay ->
                                    val act = paymentMethod == payWay
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (act) GoldPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable { paymentMethod = payWay }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = payWay,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (act) Color.Black else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            if (paymentMethod == "Credit Card") {
                                // 3D Card Flip Implementation
                                val rotation by animateFloatAsState(
                                    targetValue = if (cardFlipped) 180f else 0f,
                                    animationSpec = tween(durationMillis = 500),
                                    label = "CardFlipRotation"
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(190.dp)
                                        .graphicsLayer {
                                            rotationY = rotation
                                            cameraDistance = 8 * density
                                        }
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(Color(0xFF1E1E1E), Color(0xFF0F0F12))
                                            )
                                        )
                                        .border(1.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                        .padding(20.dp)
                                ) {
                                    if (rotation <= 90f) {
                                        // FRONT OF THE CARD
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Icon(
                                                    Icons.Default.CreditCard,
                                                    null,
                                                    tint = GoldPrimary,
                                                    modifier = Modifier.size(36.dp)
                                                )
                                                Text(
                                                    "DRIP PLATINUM",
                                                    color = GoldPrimary,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 12.sp
                                                )
                                            }

                                            Text(
                                                text = if (cardNumber.isEmpty()) "•••• •••• •••• ••••" else cardNumber,
                                                fontSize = 20.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 2.sp
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text("CARDHOLDER", fontSize = 9.sp, color = Color.Gray)
                                                    Text(
                                                        text = if (cardName.isEmpty()) "YOUR NAME" else cardName.uppercase(),
                                                        fontSize = 13.sp,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text("EXPIRES", fontSize = 9.sp, color = Color.Gray)
                                                    Text(
                                                        text = if (cardExpiry.isEmpty()) "MM/YY" else cardExpiry,
                                                        fontSize = 13.sp,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        // BACK OF THE CARD
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .graphicsLayer { rotationY = 180f },
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(36.dp)
                                                    .background(Color.Black)
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(160.dp)
                                                        .height(30.dp)
                                                        .background(Color.White.copy(alpha = 0.15f))
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .background(Color.White, RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = if (cardCvv.isEmpty()) "CVV" else cardCvv,
                                                        color = Color.Black,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                }
                                            }

                                            Text(
                                                "Drip Boutique LLC. All rights strictly reserved.",
                                                fontSize = 8.sp,
                                                color = Color.Gray,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                OutlinedTextField(
                                    value = cardName,
                                    onValueChange = { cardName = it },
                                    label = { Text("Cardholder Name") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = cardNumber,
                                    onValueChange = {
                                        cardNumber = it.take(19)
                                        cardFlipped = false
                                    },
                                    label = { Text("Card Number") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(
                                        value = cardExpiry,
                                        onValueChange = {
                                            cardExpiry = it.take(5)
                                            cardFlipped = false
                                        },
                                        label = { Text("Expiry (MM/YY)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                                    )

                                    OutlinedTextField(
                                        value = cardCvv,
                                        onValueChange = {
                                            cardCvv = it.take(3)
                                            cardFlipped = true // Flip on CVV focus!
                                        },
                                        label = { Text("CVV") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                                    )
                                }
                            } else if (paymentMethod == "COD") {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Cash on Delivery (COD) Selected", fontWeight = FontWeight.Bold, color = GoldPrimary)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            "Pay securely in physical cash upon the delivery arrival. Surcharge of $0.00 applicable.",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                // bKash
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE2125D).copy(alpha = 0.08f)),
                                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2125D), RoundedCornerShape(12.dp))
                                ) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFE2125D), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("bKash", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Text(
                                            "Redirect on placement to process mobile banking transaction instantly.",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            DripButton(text = "Go to Review Summary", onClick = { currentStep = 3 })
                        }
                    }

                    3 -> {
                        // STEP 3: REVIEW SUMMARY
                        Column {
                            Text("Review Order Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(14.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Delivery Address", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GoldPrimary)
                                    val address = addressItems.getOrNull(selectedAddressIndex) ?: addressItems.first()
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(address.fullName, fontWeight = FontWeight.Bold)
                                    Text(address.address, fontSize = 12.sp)
                                    Text("${address.city}, ${address.postalCode}", fontSize = 12.sp)
                                }
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Payment Detail", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GoldPrimary)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(paymentMethod, fontWeight = FontWeight.Bold)
                                    if (paymentMethod == "Credit Card") {
                                        Text("Card Number Ending: " + cardNumber.takeLast(4), fontSize = 12.sp)
                                    }
                                }
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Total Cost Amount", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("$${String.format("%.2f", totalAmount)}", fontWeight = FontWeight.Black, color = GoldPrimary, fontSize = 18.sp)
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            DripButton(
                                text = "Confirm & Place Order",
                                onClick = {
                                    val address = addressItems.getOrNull(selectedAddressIndex) ?: addressItems.first()
                                    val newOrderId = FirebaseService.placeOrder(address, paymentMethod, totalAmount)
                                    navController.navigate("confirmation/$newOrderId") {
                                        popUpTo("navbar")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepIndicator(step: Int, currentStep: Int, label: String) {
    val completed = currentStep >= step
    val active = currentStep == step
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (completed) GoldPrimary else MaterialTheme.colorScheme.outline)
                .border(2.dp, if (active) Color.White else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step.toString(),
                color = if (completed) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 11.sp, color = if (completed) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// --------------------------------------------------------------------------
// 9. ORDER CONFIRMATION SCREEN
// --------------------------------------------------------------------------
@Composable
fun OrderConfirmationScreen(navController: NavController, orderId: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Elegant pulsing CustomPainter Animated Checkmark
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(GoldPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Order Confirmed!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Text(
                text = "Thank you for shopping Drip! Your order is secured.",
                fontSize = 13.sp,
                color = Color(0xFF9E9E9E),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Order specifications
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF2C2C2C), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ORDER ID", color = Color.Gray, fontSize = 11.sp)
                        Text(orderId, color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ESTIMATED DELIVERY", color = Color.Gray, fontSize = 11.sp)
                        Text("In 5 Days (Today + 5)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(44.dp))

            DripButton(
                text = "Track Order Details",
                onClick = {
                    navController.navigate("my_orders")
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Continue Shopping",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .bounceClick {
                        navController.navigate("navbar") {
                            popUpTo("navbar") { inclusive = true }
                        }
                    }
                    .padding(10.dp)
            )
        }
    }
}

// --------------------------------------------------------------------------
// 10. WISHLIST SCREEN
// --------------------------------------------------------------------------
@Composable
fun WishlistScreen(navController: NavController) {
    val context = LocalContext.current
    val wishlist by FirebaseService.wishlistStream.collectAsState()

    if (wishlist.isEmpty()) {
        EmptyState(
            title = "Wishlist is Empty 🖤",
            description = "Explore our premium catalogs, search items, and heart options to save them directly to your personal locker room.",
            icon = Icons.Outlined.FavoriteBorder
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Personal Locker",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Add All to Cart",
                color = GoldPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.bounceClick {
                    FirebaseService.addAllWishlistToCart()
                    Toast.makeText(context, "All Wishlist items moved to Cart!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(wishlist) { wish ->
                val mockProd = Product(
                    id = wish.productId.toIntOrNull() ?: 99,
                    title = wish.name,
                    price = wish.price,
                    description = "Custom loaded from locker closet.",
                    category = RetrofitInstance.fallbackCategories[0],
                    images = listOf(wish.imageUrl)
                )
                ProductCard(
                    product = mockProd,
                    onNavigateDetail = {
                        navController.navigate("product_detail/${wish.productId}")
                    }
                )
            }
        }
    }
}

// --------------------------------------------------------------------------
// 11-13. PROFILE & EXTENSIONS: PROFILE, ORDERS, ADDRESSES
// --------------------------------------------------------------------------
@Composable
fun ProfileScreen(navController: NavController, darkTheme: Boolean, onThemeToggle: (Boolean) -> Unit) {
    val currentUser by FirebaseService.currentUserState.collectAsState()

    var isEditProfileOpen by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }

    LaunchedEffect(currentUser) {
        editName = currentUser?.name ?: ""
        editPhone = currentUser?.phone ?: ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Drip Profile",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Avatar details
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(GoldPrimary),
                contentAlignment = Alignment.Center
            ) {
                // Large initial
                val init = (currentUser?.name ?: "D").take(1).uppercase()
                Text(
                    text = init,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
            }

            Column {
                Text(
                    text = currentUser?.name ?: "Drip Guest",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currentUser?.email ?: "guest@drip.com",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (currentUser?.phone?.isNotEmpty() == true) {
                    Text(
                        text = currentUser?.phone ?: "",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Options Items Lists using modular Row Item Click bounds
        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ProfileOptionItem(icon = Icons.Default.Edit, label = "Edit Profile Info", action = { isEditProfileOpen = true })
        ProfileOptionItem(icon = Icons.Default.ListAlt, label = "My Orders", action = { navController.navigate("my_orders") })
        ProfileOptionItem(icon = Icons.Default.Home, label = "My Saved Addresses", action = { navController.navigate("my_addresses") })

        // Dark theme toggle switch option
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DarkMode, null, tint = GoldPrimary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text("Dark Mode", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Switch(
                checked = darkTheme,
                onCheckedChange = { onThemeToggle(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = GoldPrimary
                )
            )
        }

        ProfileOptionItem(
            icon = Icons.Default.ExitToApp,
            label = "Logout Account",
            isDestructive = true,
            action = {
                FirebaseService.logout()
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }

    // Edit profile simple screen modal dialog
    if (isEditProfileOpen) {
        AlertDialog(
            onDismissRequest = { isEditProfileOpen = false },
            title = { Text("Edit Profile Details", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Phone") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    FirebaseService.updateProfile(editName, editPhone)
                    isEditProfileOpen = false
                }) {
                    Text("Save", color = GoldPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditProfileOpen = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun ProfileOptionItem(icon: ImageVector, label: String, isDestructive: Boolean = false, action: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { action() }
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) Color(0xFFFF4D4D) else GoldPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (isDestructive) Color(0xFFFF4D4D) else MaterialTheme.colorScheme.onBackground
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            null,
            tint = if (isDestructive) Color(0xFFFF4D4D) else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrdersScreen(navController: NavController) {
    val orders by FirebaseService.ordersStream.collectAsState()
    var activeTabIdx by remember { mutableStateOf(0) } // 0: All, 1: Active, 2: Delivered, 3: Cancelled

    val filteredList = when (activeTabIdx) {
        1 -> orders.filter { it.status == "Active" }
        2 -> orders.filter { it.status == "Delivered" }
        3 -> orders.filter { it.status == "Cancelled" }
        else -> orders
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Closet Orders", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padValues ->
        Column(modifier = Modifier.fillMaxSize().padding(padValues)) {
            // Tab row selections
            TabRow(
                selectedTabIndex = activeTabIdx,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = GoldPrimary
            ) {
                val labels = listOf("All", "Active", "Delivered", "Cancelled")
                labels.forEachIndexed { index, label ->
                    Tab(
                        selected = activeTabIdx == index,
                        onClick = { activeTabIdx = index },
                        text = {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activeTabIdx == index) GoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            if (filteredList.isEmpty()) {
                EmptyState(
                    title = "No orders placed",
                    description = "You haven't requested any luxury closings under this filter. Browse trending garments now!",
                    icon = Icons.Default.ListAlt
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredList) { doc ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = "ORDER ID", fontSize = 10.sp, color = Color.Gray)
                                        Text(text = doc.orderId, fontWeight = FontWeight.Black, color = GoldPrimary)
                                    }

                                    // Status Badge selection colors
                                    val badgeBgColor = when (doc.status) {
                                        "Delivered" -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                                        "Cancelled" -> Color(0xFFFF4D4D).copy(alpha = 0.15f)
                                        else -> GoldPrimary.copy(alpha = 0.15f)
                                    }
                                    val badgeTextColor = when (doc.status) {
                                        "Delivered" -> Color(0xFF4CAF50)
                                        "Cancelled" -> Color(0xFFFF4D4D)
                                        else -> GoldPrimary
                                    }

                                    Box(
                                        modifier = Modifier
                                            .background(badgeBgColor, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = doc.status,
                                            color = badgeTextColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${doc.items.sumOf { it.quantity }} items",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Total Investment: $${String.format("%.2f", doc.totalAmount)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Estimated Arrival: In 5 Days",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAddressesScreen(navController: NavController) {
    val context = LocalContext.current
    val addressItems by FirebaseService.addressStream.collectAsState()

    var isAddingAddress by remember { mutableStateOf(false) }

    // Forms fields state
    var formFullName by remember { mutableStateOf("") }
    var formPhone by remember { mutableStateOf("") }
    var formAddress by remember { mutableStateOf("") }
    var formCity by remember { mutableStateOf("") }
    var formPostalCode by remember { mutableStateOf("") }
    var formIsDefault by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Closet Address Booker", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { isAddingAddress = true }) {
                        Icon(Icons.Default.Add, "Add New Address")
                    }
                }
            )
        }
    ) { padValues ->
        Box(modifier = Modifier.fillMaxSize().padding(padValues)) {
            if (addressItems.isEmpty()) {
                EmptyState(
                    title = "No saved addresses",
                    description = "Secure your checkout experience by adding default delivery coordinates now.",
                    icon = Icons.Default.Home,
                    buttonText = "Add Address Info",
                    onButtonClick = { isAddingAddress = true }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(addressItems) { addr ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = addr.fullName, fontWeight = FontWeight.Bold)
                                    if (addr.isDefault) {
                                        Box(
                                            modifier = Modifier
                                                .background(GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("DEFAULT", color = GoldPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(text = addr.address, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "${addr.city}, ${addr.postalCode}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = addr.phone, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (!addr.isDefault) {
                                        Text(
                                            text = "Set Default",
                                            color = GoldPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .bounceClick { FirebaseService.setDefaultAddress(addr.addressId) }
                                                .padding(8.dp)
                                        )
                                    }
                                    Text(
                                        text = "Remove",
                                        color = Color(0xFFFF4D4D),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .bounceClick { FirebaseService.deleteAddress(addr.addressId) }
                                            .padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (isAddingAddress) {
        AlertDialog(
            onDismissRequest = { isAddingAddress = false },
            title = { Text("Add Delivery Address", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = formFullName,
                        onValueChange = { formFullName = it },
                        label = { Text("Full Name Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = formPhone,
                        onValueChange = { formPhone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = formAddress,
                        onValueChange = { formAddress = it },
                        label = { Text("Address Street Info") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = formCity,
                        onValueChange = { formCity = it },
                        label = { Text("City Town") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = formPostalCode,
                        onValueChange = { formPostalCode = it },
                        label = { Text("ZIP Postal Code") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = formIsDefault,
                            onCheckedChange = { formIsDefault = it },
                            colors = CheckboxDefaults.colors(checkedColor = GoldPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Set as Default Delivery Address")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (formFullName.isNotEmpty() && formAddress.isNotEmpty() && formCity.isNotEmpty()) {
                        FirebaseService.addAddress(
                            fullName = formFullName,
                            phone = formPhone,
                            address = formAddress,
                            city = formCity,
                            postalCode = formPostalCode,
                            isDefault = formIsDefault
                        )
                        isAddingAddress = false
                        // Reset forms
                        formFullName = ""
                        formPhone = ""
                        formAddress = ""
                        formCity = ""
                        formPostalCode = ""
                        formIsDefault = false
                    }
                }) {
                    Text("Add", color = GoldPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isAddingAddress = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
