package com.springbootapp.demoo.controller;

import com.springbootapp.demoo.service.PDFSigningService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.io.ByteArrayResource;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.regex.Pattern;


@RestController
@RequestMapping("/sign")
public class PDFSignController {
    private static final Logger logger = LoggerFactory.getLogger(PDFSignController.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    @Autowired
    private PDFSigningService pdfSigningService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<ByteArrayResource> signPDF(
            @RequestParam("file") MultipartFile file,
            @RequestParam("fullName") String fullName,
            @RequestParam("email") String email) throws Exception {

        // Validate input
        logger.info("Validating input for file: {} and email: {}", file.getOriginalFilename(), email);

        if (!file.getOriginalFilename().endsWith(".pdf")) {
            logger.error("Invalid file type. Only PDF is supported.");
            throw new IllegalArgumentException("Invalid file type. Only PDF is supported.");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            logger.error("Invalid email format: {}", email);
            throw new IllegalArgumentException("Invalid email format.");
        }

        // Call the signing service
        byte[] signedPdf = pdfSigningService.signPDF(file, fullName);

        // Prepare the response
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("signed-document.pdf").build());

        ByteArrayResource resource = new ByteArrayResource(signedPdf);

        logger.info("PDF signing process completed successfully for file: {}", file.getOriginalFilename());

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

}
