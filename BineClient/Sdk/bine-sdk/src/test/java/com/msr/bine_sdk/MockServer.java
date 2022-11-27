package com.msr.bine_sdk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;

public class MockServer {
    private final Dispatcher dispatcher = new Dispatcher() {
        @Override
        public MockResponse dispatch(RecordedRequest request) {
            switch (request.getPath()) {
                case "/list/files/MSR/root":
                    return getListResponse("root").setResponseCode(200);
                case "/list/files/MSR/ars-talks":
                    return getListResponse("ars-talks").setResponseCode(200);
                case "/list/files/MSR/ars-talks-ARS%202020%20Fairness%20Transparency%20and%20Privacy%20in%20Digital%20Systems":
                    return getListResponse("ars-talks-ARS 2020 Fairness Transparency and Privacy in Digital Systems").setResponseCode(200);
                case "/metadata/MSR/ars-talks-ARS%202020%20Fairness%20Transparency%20and%20Privacy%20in%20Digital%20Systems":
                    return getSingleResponse("ars-talks-ARS 2020 Fairness Transparency and Privacy in Digital Systems").setResponseCode(200);
                case "/auth/login":
                    return getLoginResponse(request);
                case "/validate":
                    return getOTPResponse(request);
                case "/auth/logout":
                    return getLoginResponse(request);//TODO:
            }
            return new MockResponse().setResponseCode(404);
        }
    };

    public MockWebServer getServer() throws IOException {
        MockWebServer server = new MockWebServer();
        server.setDispatcher(dispatcher);
        server.start();
        return server;
    }

    private MockResponse getSingleResponse(String ID) {
        MockResponse response = new MockResponse();
        response.setBody(new Buffer());
        MockFolder mockFolder = getMockFolder(ID, 100 * 1024 * 1024, false);
        writeMockListChildToResponse(response, mockFolder);
        return response;
    }

    private MockResponse getListResponse(String parent) {
        MockResponse response = new MockResponse();
        List<MockFolder> children = getMockChildren(parent);
        response.setBody(new Buffer());
        // Write number of children
        response.setBody(response.getBody().writeInt(children.size()));
        System.out.println("Size of children is: " + children.size() + " passed parent: " + parent);
        for (MockFolder mockFolder : children) {
            response.setBody(response.getBody()
                    .writeInt(mockFolder.ID.getBytes().length)
                    .write(mockFolder.ID.getBytes())
                    // write has Children
                    .writeInt(mockFolder.hasChildren ? 1 : 0));
            writeMockListChildToResponse(response, mockFolder);
        }
        System.out.println("returning response of size: " + response.getBody().size());
        return response;
    }

    private void writeMockListChildToResponse(MockResponse response, MockFolder mockFolder) {
        // write childID
        response.setBody(response.getBody()
                // write folder size
                .writeLong(mockFolder.sizeOfFolder)
                // write metadata files size
                .writeInt(mockFolder.metadataFiles.size()));

        // write metadata files
        for (MockMetadataFile mockMetadataFile : mockFolder.metadataFiles) {
            // write file name
            response.setBody(response.getBody()
                    .writeInt(mockMetadataFile.name.getBytes().length)
                    .write(mockMetadataFile.name.getBytes())
                    // write file size
                    .writeLong(mockMetadataFile.size));

            // write file bytes
            int written = 0;
            while (written < mockMetadataFile.size) {
                int toWrite = 1024 < mockMetadataFile.size - written ? (int) (mockMetadataFile.size - written) : 1024;
                response.setBody(response.getBody().write(new byte[toWrite]));
                written += toWrite;
            }
        }
    }

    private List<MockFolder> getMockChildren(String parent) {
        List<MockFolder> result = new ArrayList<>();
        List<String> children;
        switch (parent) {
            case "root":
                children = new ArrayList<>();
                children.add("ars-talks");
                for (String kid : children) {
                    result.add(getMockFolder(kid, 0, true));
                }
                break;
            case "ars-talks":
                children = new ArrayList<>();
                children.add("ars-talks-ARS 2020 Fairness Transparency and Privacy in Digital Systems");
                for (String kid : children) {
                    result.add(getMockFolder(kid, 100 * 1024 * 1024, false));
                }
                break;
        }
        return result;
    }

    private MockFolder getMockFolder(String ID, long folderContentSize, boolean hasChildren) {
        MockFolder child = new MockFolder();
        child.setID(ID);
        child.setSizeOfFolder(folderContentSize);
        child.metadataFiles.add(getMockMetadataFile(ID, "cover.jpg"));
        child.metadataFiles.add(getMockMetadataFile(ID, "thumbnail.jpg"));
        child.metadataFiles.add(getMockMetadataFile(ID, "bine_metadata-json"));
        child.setHasChildren(hasChildren);
        return child;
    }

    private MockMetadataFile getMockMetadataFile(String ID, String name) {
        MockMetadataFile mockMetadataFile = new MockMetadataFile();
        mockMetadataFile.setName(name);
        // TODO: Based on name and ID, if needed incorporate actual files here instead of random file
        // By adding another field in MockMetadataFile and fill it with actual file contents
        // NOTE: for now, random 10KB will be sent to the client
        mockMetadataFile.setSize(10 * 1024);
        return mockMetadataFile;
    }

    private static class MockFolder {
        String ID;
        List<MockMetadataFile> metadataFiles = new ArrayList<>();
        long sizeOfFolder;

        void setHasChildren(boolean hasChildren) {
            this.hasChildren = hasChildren;
        }

        boolean hasChildren;

        void setID(String ID) {
            this.ID = ID;
        }

        void setSizeOfFolder(long sizeOfFolder) {
            this.sizeOfFolder = sizeOfFolder;
        }
    }

    private static class MockMetadataFile {
        String name;
        long size;

        void setName(String name) {
            this.name = name;
        }

        void setSize(long size) {
            this.size = size;
        }
    }

    private MockResponse getLoginResponse(RecordedRequest request) {
        MockResponse response = new MockResponse();
        String requestBody = request.getBody().toString();
        if (requestBody.contains("9000000000")) {
            response.setBody("{\"token\":\"abcdefghi\"}");
            response.setResponseCode(200);
        } else if (requestBody.contains("9000000001")) {
            response.setBody("{\"details\":\"Validation pending\"}");
            response.setResponseCode(200);
        } else if (requestBody.contains("9000000002")) {
            response.setBody("{'status':True,'statusCode':204,'details':'OTP sent. Please validate the same.'}");
            response.setResponseCode(200);
        } else {
            response.setResponseCode(403);
        }
        return response;
    }

    private MockResponse getOTPResponse(RecordedRequest request) {
        MockResponse response = new MockResponse();
        String requestBody = request.getBody().toString();
        if (requestBody.contains("123456")) {
            response.setBody("{'details':'OTP expired.Generated a new OTP'}");
            response.setResponseCode(400);
        } else if (requestBody.contains("123457")) {
            response.setBody("{'status':True,'token':'abcdefghi'}");
            response.setResponseCode(200);
        } else if (requestBody.contains("123458")) {
            response.setBody("{'status':False,'details':'Incorrect Credentials provided'}");
            response.setResponseCode(200);
        } else {
            response.setResponseCode(403);
        }
        return response;
    }
}
