# This bot has features like:
- Talking randomly (Just concatenating random words that bot memorised)
- Sending random stickers that you can set
- Reacting with random reactions to user messages
- Saving and loading user messages to disk

## Also there is some configurations:
- reactionEmojisByEqualsICAndEmoji (bot will set reactions if message = word that you set)
- stickerIds (stickers that bot will randomly send) use https://github.com/unknownredgreen/debugging-tg-bot to get sticker ids
- reactionEmojis (bot will randomly react to messages)

## Config format
- reactionEmojisByEqualsICAndEmoji:word=emoji,two words=emoji
- stickerIds:firststickerid,secondstickerid,thirdstickerid
- reactionEmojis:emoji,secondemoji

## Config example
reactionEmojisByEqualsICAndEmoji:test=👨‍💻,testing multiple words=👨‍💻

stickerIds:CAACAgEAAyEFAASuD3AtAAEFbS1pW9R8aHy0A06w2wN_a8YF5zBrGgACXwIAAvZ9MUfDGmLnYQ-B3jgE

reactionEmojis:👨‍💻,🎅

## Telegram limitations
Bots can react not with all reactions that users can

## Exit codes
0 - OK

1 - generic error

2 - config error