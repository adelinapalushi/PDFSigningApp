package com.springbootapp.demoo.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceGray;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.signatures.*;
import com.springbootapp.demoo.util.FileUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;

@Service
public class PDFSigningService {
    private static final Logger logger = LoggerFactory.getLogger(PDFSigningService.class);

    @Autowired
    private FileUtil fileUtil;

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public byte[] signPDF(MultipartFile pdfFile, String signerName) throws Exception {
        logger.info("Starting PDF signing process for file: {}", pdfFile.getOriginalFilename());

        Path tempFile = fileUtil.saveFile(pdfFile.getBytes(), pdfFile.getOriginalFilename());
        logger.info("File saved to temporary storage: {}", tempFile.toString());

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Files.readAllBytes(tempFile));
             ByteArrayOutputStream signedPdfOutput = new ByteArrayOutputStream()) {

            PdfReader reader = new PdfReader(inputStream);
            PdfSigner signer = new PdfSigner(reader, signedPdfOutput, new StampingProperties());
            logger.info("PDFSigner initialized.");

            // Load keystore and private key
            String keystorePath = "C:/Users/User/Downloads/demoo/demoo/keystore.jks";
            String keystorePassword = "adelina96";
            String keyAlias = "mykey";
            String keyPassword = "adelina96";

            KeyStore keyStore = KeyStore.getInstance("JKS");
            try (InputStream keystoreStream = new FileInputStream(keystorePath)) {
                keyStore.load(keystoreStream, keystorePassword.toCharArray());
                logger.info("Keystore loaded.");
            }

            PrivateKey privateKey = (PrivateKey) keyStore.getKey(keyAlias, keyPassword.toCharArray());
            Certificate[] chain = keyStore.getCertificateChain(keyAlias);
            logger.info("Private key and certificate chain retrieved from keystore.");

            // Get the dimensions of the first page
            float pageWidth = signer.getDocument().getFirstPage().getPageSize().getWidth();

            // Create signature appearance
            PdfSignatureAppearance appearance = signer.getSignatureAppearance()
                    .setReason("Document signed by " + signerName)
                    .setLocation("Location")
                    .setPageRect(new Rectangle(pageWidth - 200 - 36, 10, 250, 50))
                    .setPageNumber(1);
            logger.info("Signature appearance created.");

            // Create the appearance for name and image
            PdfFormXObject n2 = appearance.getLayer2();
            Canvas canvas = new Canvas(n2, signer.getDocument());

            // Add text signature
            Paragraph paragraph = new Paragraph(signerName)
                    .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE)) // Arial, Italic
                    .setFontSize(10)
                    .setFontColor(DeviceGray.BLACK);
            canvas.showTextAligned(paragraph, 100, 30, TextAlignment.LEFT);
            logger.info("Text signature added to PDF.");

            // Add signature image
            Image signatureImage = new Image(ImageDataFactory.create("C:/Users/User/Downloads/signature.png"));
            signatureImage.scaleToFit(100, 100);
            signatureImage.setFixedPosition(160, 25);
            canvas.add(signatureImage);
            canvas.close();
            logger.info("Signature image added to PDF.");

            IExternalSignature signature = new PrivateKeySignature(privateKey, DigestAlgorithms.SHA256, BouncyCastleProvider.PROVIDER_NAME);
            IExternalDigest digest = new BouncyCastleDigest();
            signer.signDetached(digest, signature, chain, null, null, null, 0, PdfSigner.CryptoStandard.CMS);

            logger.info("PDF signing successful for file: {}", pdfFile.getOriginalFilename());
            return signedPdfOutput.toByteArray();
        } catch (Exception e) {
            logger.error("Error during signing process for file: {}", pdfFile.getOriginalFilename(), e);
            throw new Exception("Error signing PDF", e);
        }
    }
}


