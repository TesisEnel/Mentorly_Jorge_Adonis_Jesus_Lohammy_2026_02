package com.sagrd.mentorly.presentation.admin.peerreview.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagrd.mentorly.domain.model.peerreview.PeerReview
import com.sagrd.mentorly.ui.theme.MentorlyTheme

@Composable
fun AdminPeerReviewListScreen(
    onBackClick: () -> Unit,
    onPeerReviewClick: (String) -> Unit,
    viewModel: AdminPeerReviewListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredPeerReviews by viewModel.filteredPeerReviews.collectAsStateWithLifecycle()

    AdminPeerReviewListContent(
        uiState = uiState,
        peerReviews = filteredPeerReviews,
        onBackClick = onBackClick,
        onPeerReviewClick = onPeerReviewClick,
        onSearchChanged = { viewModel.onEvent(AdminPeerReviewListUiEvent.SearchChanged(it)) },
        onFilterChanged = { viewModel.onEvent(AdminPeerReviewListUiEvent.FilterChanged(it)) },
        onRefresh = { viewModel.onEvent(AdminPeerReviewListUiEvent.Refresh) },
        onClearError = { viewModel.onEvent(AdminPeerReviewListUiEvent.ClearError) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminPeerReviewListContent(
    uiState: AdminPeerReviewListUiState,
    peerReviews: List<PeerReview>,
    onBackClick: () -> Unit,
    onPeerReviewClick: (String) -> Unit,
    onSearchChanged: (String) -> Unit,
    onFilterChanged: (PeerReviewFilter) -> Unit,
    onRefresh: () -> Unit,
    onClearError: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Revisiones por pares") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(imageVector = Icons.Default.FilterList, contentDescription = "Recargar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Buscar en comentarios o ID...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            ScrollableTabRow(
                selectedTabIndex = uiState.selectedFilter.ordinal,
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                PeerReviewFilter.entries.forEach { filter ->
                    Tab(
                        selected = uiState.selectedFilter == filter,
                        onClick = { onFilterChanged(filter) },
                        text = {
                            Text(
                                text = when (filter) {
                                    PeerReviewFilter.All -> "Todas"
                                    PeerReviewFilter.Approved -> "Aprobadas"
                                    PeerReviewFilter.Rejected -> "Rechazadas"
                                }
                            )
                        }
                    )
                }
            }

            if (uiState.isLoading && !uiState.isRefreshing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.errorMessage != null) {
                ErrorContent(
                    message = uiState.errorMessage,
                    onRetry = onRefresh,
                    onDismiss = onClearError
                )
            } else if (peerReviews.isEmpty()) {
                EmptyContent()
            } else {
                PeerReviewList(
                    peerReviews = peerReviews,
                    onPeerReviewClick = onPeerReviewClick
                )
            }
        }
    }
}

@Composable
private fun PeerReviewList(
    peerReviews: List<PeerReview>,
    onPeerReviewClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(peerReviews, key = { it.id }) { review ->
            PeerReviewItem(
                review = review,
                onClick = { onPeerReviewClick(review.id) }
            )
        }
    }
}

@Composable
private fun PeerReviewItem(
    review: PeerReview,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ID: ${review.id.take(8)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                StatusLabel(isApproved = review.isApproved)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = review.feedbackComment,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = review.createdAt,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun StatusLabel(isApproved: Boolean) {
    Surface(
        color = if (isApproved) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
        contentColor = if (isApproved) Color(0xFF2E7D32) else Color(0xFFC62828),
        shape = CircleShape
    ) {
        Text(
            text = if (isApproved) "Aprobada" else "Rechazada",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
        Row(modifier = Modifier.padding(top = 16.dp)) {
            Button(onClick = onRetry) { Text("Reintentar") }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    }
}

@Composable
private fun EmptyContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No se encontraron revisiones.")
    }
}

@Preview(showBackground = true)
@Composable
fun AdminPeerReviewListScreenPreview() {
    MentorlyTheme {
        AdminPeerReviewListContent(
            uiState = AdminPeerReviewListUiState(),
            peerReviews = listOf(
                PeerReview("1", "sub-1", "rev-1", true, "Excelente trabajo, cumple con todo.", "2026-08-14"),
                PeerReview("2", "sub-2", "rev-2", false, "Falta completar el punto 3.", "2026-08-14")
            ),
            onBackClick = {},
            onPeerReviewClick = {},
            onSearchChanged = {},
            onFilterChanged = {},
            onRefresh = {},
            onClearError = {}
        )
    }
}
