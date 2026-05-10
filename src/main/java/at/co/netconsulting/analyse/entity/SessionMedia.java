package at.co.netconsulting.analyse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.OffsetDateTime;

@Entity
@Table(name = "session_media")
@Getter
@Setter
@NoArgsConstructor
public class SessionMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "media_id")
    private Integer mediaId;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "media_uuid", nullable = false, unique = true)
    private String mediaUuid;

    @Column(name = "media_type", nullable = false)
    private String mediaType;

    @Column(name = "file_extension", nullable = false)
    private String fileExtension;

    @Column(name = "original_filename")
    private String originalFilename;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "thumbnail_generated")
    private Boolean thumbnailGenerated;

    private String caption;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
