package com.xiaomi.test.xmtest.fileoperate;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import javax.servlet.ServletException;

import net.sf.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.logging.Logger;




/**
 * Created by myersguo on 2016/6/30.
 */
public class ConfigUpdate extends Builder {

    public String filePath = "";
    public String fileContent = "";
    public String fileOption = "overWrite";



    @DataBoundConstructor
    public  ConfigUpdate(String filePath, String fileContent, String fileOption) {
        this.filePath = filePath;
        this.fileContent = fileContent;
        this.fileOption = fileOption;
    }

    @DataBoundSetter
    public void setFilePath(String outputFile) {
        this.filePath = outputFile;
    }
    @DataBoundSetter
    public void setFileOption(String fileOption){this.fileOption = fileOption;}
    @DataBoundSetter
    public void setFileContent(String fileContent){this.fileContent = fileContent;}



    public String getFilePath() {
        return filePath;
    }
    public String getFileContent() {
        return fileContent;
    }
    public String getFileOption() {
        return fileOption;
    }

    @Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException,IOException {
        final PrintStream logger = listener.getLogger();


        if(filePath == null || fileOption.isEmpty()){
            logger.println("文件路径、操作方式不能为空");
            throw new IllegalStateException("文件路径、操作方式不能为空");
        }

        try {
            String existingFileContents = "";
            String finalFileContent = "";
            String eol = System.getProperty("line.separator");
            FilePath textFile = new FilePath(new File(filePath));

            if(!textFile.exists()){
                listener.getLogger().println(String.format("File does not exist at '%s', new file will be created.", filePath));
                finalFileContent = fileContent;
            }

            if(!textFile.exists()){
                listener.getLogger().println(String.format("File does not exist at '%s', new file will be created.", filePath));
                finalFileContent = fileContent;
            }
            else{
                listener.getLogger().println(String.format("File already exists at '%s', selected write option is '%s'", filePath,fileOption));
                existingFileContents = textFile.readToString();

                if(fileOption.equalsIgnoreCase("overWrite")){
                    finalFileContent = fileContent;

                }else if(fileOption.equalsIgnoreCase("appendToEnd")){

                    if(existingFileContents.endsWith(eol)){
                        finalFileContent = existingFileContents.concat(fileContent);
                    }else{
                        finalFileContent = existingFileContents.concat(eol + fileContent);
                    }

                }else if(fileOption.equalsIgnoreCase("insertAtStart")){

                    if(existingFileContents.startsWith(eol)){
                        finalFileContent = fileContent.concat(existingFileContents);
                    }else{
                        finalFileContent = fileContent.concat(eol + existingFileContents);
                    }
                }

                textFile.deleteContents();
            }

            finalFileContent = finalFileContent.replaceAll("\n", System.lineSeparator());

            textFile.write(finalFileContent, "UTF-8");
        }catch (Exception e) {

            listener.getLogger().println("Failed to create/update file. " + e.getMessage());
            e.printStackTrace(listener.getLogger());
            return false;
        }

        listener.getLogger().println("File successfully created/updated at "+ filePath);
        return true;
    }


    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }


    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public DescriptorImpl() {
            load();
        }


        public FormValidation doCheckFilePath(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("请设置文件路径");

            return FormValidation.ok();
        }

        public FormValidation doCheckFileContent(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("请输入文件内容");

            return FormValidation.ok();
        }

        public FormValidation doCheckFileOption(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("请选择文件操作方式：默认为覆盖.");
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }


        public String getDisplayName() {
            return "文件更新";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            save();
            return super.configure(req, formData);
        }
    }
}
