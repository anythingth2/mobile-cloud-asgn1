/*
 *
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.magnum.dataup;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@RestController
public class VideoController {
    public Map<Long, Video> videos = new HashMap();

    @GetMapping("/video")
    public List<Video> getVideos() {
        return new ArrayList<Video>(this.videos.values());
    }

    @PostMapping("/video")
    public Video addVideos(@RequestBody Video video) {
        if ( video.getTitle() == null || video.getSubject() == null || video.getDuration() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        video.setId(this.videos.size()+1);
        video.setDataUrl("http://localhost:8080/video/" + video.getId() + "/data");

        System.out.println("add video " + video.getId());
        this.videos.put(video.getId(), video);
        return video;
    }

    @PostMapping("/video/{id}/data")
    public VideoStatus uploadVideoFile(@PathVariable long id, @RequestParam("data") MultipartFile file) throws IOException {
        if (!this.videos.containsKey(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Video video = this.videos.get(id);
        VideoFileManager.getInstance().saveVideoData(video, file.getInputStream());
        return new VideoStatus(VideoStatus.VideoState.READY);
    }

    @GetMapping("/video/{id}/data")
    public byte[] getVideoData(@PathVariable long id) throws IOException {
        System.out.println("add video data "+ id);
        if (!this.videos.containsKey(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        Video video = this.videos.get(id);
        if (!VideoFileManager.getInstance().hasVideoData(video))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        VideoFileManager.getInstance().copyVideoData(video, outputStream);
        return outputStream.toByteArray();
    }
}
