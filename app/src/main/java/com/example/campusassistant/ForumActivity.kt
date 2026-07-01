package com.example.campusassistant

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusassistant.data.AppDatabase
import com.example.campusassistant.data.ForumPost
import com.example.campusassistant.data.ForumReply
import com.example.campusassistant.ui.theme.CampusAssistantTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ForumActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val forumDao = AppDatabase.getDatabase(this).forumPostDao()
        val replyDao = AppDatabase.getDatabase(this).forumReplyDao()

        setContent {
            CampusAssistantTheme {
                ForumScreen(
                    forumDao = forumDao,
                    replyDao = replyDao,
                    onShowToast = { msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }
                )
            }
        }
    }
}

private val CATEGORIES = listOf("全部", "学习", "生活", "活动", "求助", "其他")

private fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000 -> "刚刚"
        diff < 3600_000 -> "${diff / 60_000}分钟前"
        diff < 86400_000 -> "${diff / 3600_000}小时前"
        diff < 604800_000 -> "${diff / 86400_000}天前"
        else -> {
            val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

// ==================== 主入口 ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumScreen(
    forumDao: com.example.campusassistant.data.ForumPostDao,
    replyDao: com.example.campusassistant.data.ForumReplyDao,
    onShowToast: (String) -> Unit
) {
    // selectedPostId == null → 帖子列表页；!= null → 帖子详情页
    var selectedPostId by remember { mutableStateOf<Long?>(null) }

    if (selectedPostId == null) {
        PostListScreen(
            forumDao = forumDao,
            onPostClick = { postId -> selectedPostId = postId },
            onShowToast = onShowToast
        )
    } else {
        PostDetailScreen(
            postId = selectedPostId!!,
            forumDao = forumDao,
            replyDao = replyDao,
            onBack = { selectedPostId = null },
            onShowToast = onShowToast
        )
    }
}

// ==================== 帖子列表页 ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostListScreen(
    forumDao: com.example.campusassistant.data.ForumPostDao,
    onPostClick: (Long) -> Unit,
    onShowToast: (String) -> Unit
) {
    var posts by remember { mutableStateOf<List<ForumPost>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf("全部") }
    var showAddDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun loadPosts() {
        scope.launch(Dispatchers.IO) {
            val result = if (selectedCategory == "全部") {
                forumDao.getAllPosts()
            } else {
                forumDao.getPostsByCategory(selectedCategory)
            }
            withContext(Dispatchers.Main) { posts = result }
        }
    }

    LaunchedEffect(selectedCategory) { loadPosts() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("校园论坛", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("+", fontSize = 28.sp, fontWeight = FontWeight.Bold,
                     color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // 分类标签
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CATEGORIES) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            if (posts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("暂无帖子，点击 + 发布第一条吧",
                         color = MaterialTheme.colorScheme.onSurfaceVariant,
                         fontSize = 15.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(posts, key = { it.id }) { post ->
                        PostCard(post, onClick = { onPostClick(post.id) })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPostDialog(
            onDismiss = { showAddDialog = false },
            onPost = { title, category, content ->
                scope.launch(Dispatchers.IO) {
                    forumDao.insertPost(ForumPost(title = title, content = content,
                                                   author = null, category = category))
                    withContext(Dispatchers.Main) {
                        onShowToast("发布成功！")
                        showAddDialog = false
                        loadPosts()
                    }
                }
            }
        )
    }
}

// ==================== 帖子卡片 ====================

@Composable
fun PostCard(post: ForumPost, onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(post.title, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                 maxLines = 1, overflow = TextOverflow.Ellipsis,
                 color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(6.dp))
            Text(post.content, fontSize = 14.sp, maxLines = 3,
                 overflow = TextOverflow.Ellipsis,
                 color = MaterialTheme.colorScheme.onSurfaceVariant,
                 lineHeight = 20.sp)
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer) {
                        Text(post.category, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                             fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    if (!post.author.isNullOrBlank()) {
                        Spacer(Modifier.width(8.dp))
                        Text(post.author, fontSize = 12.sp,
                             color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(formatTime(post.publishTime), fontSize = 12.sp,
                     color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ==================== 帖子详情页（正文 + 评论 + 回复） ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: Long,
    forumDao: com.example.campusassistant.data.ForumPostDao,
    replyDao: com.example.campusassistant.data.ForumReplyDao,
    onBack: () -> Unit,
    onShowToast: (String) -> Unit
) {
    var post by remember { mutableStateOf<ForumPost?>(null) }
    var replies by remember { mutableStateOf<List<ForumReply>>(emptyList()) }
    var replyCount by remember { mutableIntStateOf(0) }
    var replyText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    fun loadDetail() {
        scope.launch(Dispatchers.IO) {
            val allPosts = forumDao.getAllPosts()
            val p = allPosts.find { it.id == postId }
            val r = replyDao.getRepliesByPostId(postId)
            val count = replyDao.getReplyCount(postId)
            withContext(Dispatchers.Main) {
                post = p
                replies = r
                replyCount = count
            }
        }
    }

    LaunchedEffect(postId) { loadDetail() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(post?.title ?: "帖子详情", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("← 返回") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            // 底部回复输入栏
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = { Text("写下你的评论...") },
                        modifier = Modifier.weight(1f),
                        maxLines = 3,
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (replyText.isBlank()) return@Button
                            scope.launch(Dispatchers.IO) {
                                val maxFloor = replyDao.getMaxFloorNumber(postId)
                                val reply = ForumReply(
                                    postId = postId,
                                    content = replyText.trim(),
                                    author = null,
                                    floorNumber = maxFloor + 1
                                )
                                replyDao.insertReply(reply)
                                withContext(Dispatchers.Main) {
                                    onShowToast("回复成功！")
                                    replyText = ""
                                    loadDetail()
                                }
                            }
                        },
                        enabled = replyText.isNotBlank(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text("发送")
                    }
                }
            }
        }
    ) { innerPadding ->
        if (post == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 帖子正文卡片
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(post!!.title, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                                 color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer) {
                                    Text(post!!.category,
                                         modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                         fontSize = 12.sp,
                                         color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(formatTime(post!!.publishTime), fontSize = 12.sp,
                                     color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(Modifier.height(12.dp))
                            Text(post!!.content, fontSize = 15.sp,
                                 color = MaterialTheme.colorScheme.onSurface,
                                 lineHeight = 24.sp)
                        }
                    }
                }

                // 评论标题
                item {
                    Text("全部评论 ($replyCount)",
                         fontSize = 16.sp,
                         fontWeight = FontWeight.Bold,
                         color = MaterialTheme.colorScheme.onSurface,
                         modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                }

                // 评论列表
                if (replies.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center) {
                            Text("暂无评论，抢个沙发吧 🛋️",
                                 color = MaterialTheme.colorScheme.onSurfaceVariant,
                                 fontSize = 14.sp)
                        }
                    }
                } else {
                    items(replies, key = { it.id }) { reply ->
                        ReplyCard(reply)
                    }
                }
            }
        }
    }
}

// ==================== 评论卡片（带楼层号） ====================

@Composable
fun ReplyCard(reply: ForumReply) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 楼层号 + 作者
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "#${reply.floorNumber}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    if (!reply.author.isNullOrBlank()) {
                        Spacer(Modifier.width(8.dp))
                        Text(reply.author, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                             color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                Text(formatTime(reply.replyTime), fontSize = 12.sp,
                     color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(6.dp))
            Text(reply.content, fontSize = 14.sp,
                 color = MaterialTheme.colorScheme.onSurface,
                 lineHeight = 20.sp)
        }
    }
}

// ==================== 发帖弹窗（保持不变） ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPostDialog(
    onDismiss: () -> Unit,
    onPost: (title: String, category: String, content: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("学习") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("发布新帖", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = title, onValueChange = { if (it.length <= 30) title = it },
                    label = { Text("标题") },
                    supportingText = { Text("${title.length}/30") },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                ExposedDropdownMenuBox(expanded = dropdownExpanded,
                                        onExpandedChange = { dropdownExpanded = it }) {
                    OutlinedTextField(
                        value = selectedCategory, onValueChange = {}, readOnly = true,
                        label = { Text("分类") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = dropdownExpanded,
                                         onDismissRequest = { dropdownExpanded = false }) {
                        CATEGORIES.filter { it != "全部" }.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = { selectedCategory = cat; dropdownExpanded = false }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = content, onValueChange = { content = it },
                    label = { Text("正文") }, minLines = 4, maxLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || content.isBlank()) return@Button
                    onPost(title.trim(), selectedCategory, content.trim())
                },
                enabled = title.isNotBlank() && content.isNotBlank()
            ) { Text("发布") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
