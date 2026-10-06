package com.syncforge.app.ui.screens.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PersonSearch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncforge.app.ui.components.BottomBarInset
import com.syncforge.app.ui.components.EmptyState
import com.syncforge.app.ui.components.ForgeChip
import com.syncforge.app.ui.components.ForgeSearchBar
import com.syncforge.app.ui.components.ForgeTopBar
import com.syncforge.app.ui.components.ProfileCard
import com.syncforge.app.ui.components.SkeletonListCard
import com.syncforge.app.ui.theme.Space
import com.syncforge.app.viewmodel.TeamViewModel

@Composable
fun TeamScreen(
    onBack: () -> Unit,
    onMessage: (String) -> Unit,
    viewModel: TeamViewModel = viewModel(),
) {
    val members by viewModel.members.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val role by viewModel.roleFilter.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()

    val online = members.count { it.user.presence.name == "Online" }

    Column(Modifier.fillMaxSize()) {
        ForgeTopBar(
            title = "Team",
            eyebrow = "Northwind Labs",
            subtitle = "${members.size} people · $online online now",
            navIcon = Icons.AutoMirrored.Rounded.ArrowBack,
            onNavClick = onBack,
        )

        Column(Modifier.padding(horizontal = Space.gutter)) {
            ForgeSearchBar(
                value = query,
                onValueChange = viewModel::setQuery,
                placeholder = "Search by name, role or skill",
            )
            Spacer(Modifier.height(Space.sm))
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = Space.gutter),
            horizontalArrangement = Arrangement.spacedBy(Space.xs),
        ) {
            item {
                ForgeChip("Everyone", selected = role == null, onClick = { viewModel.setRole(null) })
            }
            items(viewModel.roleGroups) { group ->
                ForgeChip(
                    label = group,
                    selected = role == group,
                    onClick = { viewModel.setRole(if (role == group) null else group) },
                )
            }
        }

        Spacer(Modifier.height(Space.sm))

        when {
            loading -> LazyColumn(
                contentPadding = PaddingValues(horizontal = Space.gutter),
                verticalArrangement = Arrangement.spacedBy(Space.sm),
            ) {
                items(5) { SkeletonListCard(lines = 4) }
            }

            members.isEmpty() -> EmptyState(
                icon = Icons.Rounded.PersonSearch,
                title = "No teammates found",
                message = "Nobody matches that search. Try a different name, role or skill.",
                actionLabel = "Clear search",
                onAction = {
                    viewModel.setQuery("")
                    viewModel.setRole(null)
                },
                modifier = Modifier.fillMaxSize(),
            )

            else -> LazyColumn(
                contentPadding = PaddingValues(
                    start = Space.gutter, end = Space.gutter, bottom = BottomBarInset,
                ),
                verticalArrangement = Arrangement.spacedBy(Space.sm),
            ) {
                items(members, key = { it.user.id }) { card ->
                    ProfileCard(
                        user = card.user,
                        currentTask = card.currentTask?.title,
                        openTasks = card.openTasks,
                        onClick = {
                            onMessage("${card.user.name} · ${card.projectNames.size} projects")
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
