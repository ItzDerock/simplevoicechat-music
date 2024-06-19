# Simple Voice Chat Music Player

Enjoy music with your friends. This mod allows you to stream youtube, soundcloud, bandcamp, vimeo, twitch, mp3, flac, wav, m3u, and more into SimpleVoiceChat groups.
Powered by the lightweight [lavaplayer](https://github.com/lavalink-devs/lavaplayer) library.

https://github.com/ItzDerock/simplevoicechat-music/assets/14848722/c974d7a7-26a7-44b9-9c8a-b6d6722a8582

## Commands

- `/music play <song>` - Searches and queues the first result
- `/music search <song>` - Lists all results and lets you choose which you want to queue
- `/music now-playing` - Shows the current song
- `/music queue` - Shows the queue
- `/music skip` - Skips the current song
- `/music pause` - Pauses the current song
- `/music resume` - Resumes the current song
- `/music stop` - Stops the current song and clears the queue
- `/music volume <int;1-100>` - Sets the volume
- `/music kill` - use when something goes wrong and you want to restart the plugin without restarting the server
- `/music bassboost <float;0-200>` - adds bass boost

Song can be a soundcloud URL, Youtube URL, bandcamp URL, etc or it can be just a search term. By default, it will search on YouTube. You can force it to search on soundcloud by using the query `"scsearch: your search terms"`. Lavaplayer also supports YouTube Music, though it wasn't very reliable in my testing. To search YouTube Music, use the query `"ytmsearch: your search terms"`.

## Customization

Currently, no options are customizable as this was made for private use, but I thought I'd open-source it since
others may find it useful.

Feel free to PR a refactor that adds in more customization.

## Bugs

Open an issue, but I can't guarantee I'll get around to fixing it.
