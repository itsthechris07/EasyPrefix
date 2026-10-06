// shows the site name as the wordmark of the banner: "Easy" in white, "Prefix" in gold
document$.subscribe(() => {
  document.querySelectorAll(".md-header__topic:first-child .md-ellipsis").forEach((title) => {
    if (title.textContent.trim() === "EasyPrefix") {
      title.innerHTML = 'Easy<span class="ep-wordmark-gold">Prefix</span>';
    }
  });
});
