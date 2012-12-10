package com.mediatek.weather3dwidget;

public final class TimeZoneTransition {
    private TimeZoneTransition() {}

    public static String getGmtTz(String glbTz) {
        for (TzTransition p : TzTransition.values()) {
            if (glbTz != null && glbTz.equalsIgnoreCase(p.mGlobalTz)) {
                return p.mGmtTz;
            }
        }
        return null;
    }

    public static enum TzTransition {

        NZDT("NZDT", "GMT+13:00"),
        IDLE("IDLE","GMT+12:00"),
        NZST("NZST","GMT+12:00"),
        NZT("NZT","GMT+12:00"),
        AESST("AESST","GMT+11:00"),
        NCT("NCT","GMT+11:00"), // New Caledonia Time; Noumea, New Celedonia
        ACSST("ACSST","GMT+10:30"),
        CADT("CADT","GMT+10:30"),
        SADT("SADT","GMT+10:30"),
        AEST("AEST","GMT+10:00"),
        EAST("EAST","GMT+10:00"),
        GST("GST","GMT+10:00"),
        LIGT("LIGT","GMT+10:00"),
        PGT("PGT","GMT+10:00"), // Papua New Guinea Time; Port Moresby, Papua New Guinea
        ACST("ACST", "GMT+9:30"), // Australian Central Standard Time; Adelaide, Australia
        CAST("CAST","GMT+9:30"),
        SAT("SAT","GMT+9:30"),
        WDT("WDT","GMT+9:00"),
        AWSST("AWSST","GMT+9:00"),
        JST("JST","GMT+9:00"),
        KST("KST","GMT+9:00"),
        MHT("MHT","GMT+9:00"),
        MT("MT","GMT+8:30"),
        AWST("AWST","GMT+8:00"),
        BNT("BNT","GMT+8:00"), // Brunei Darussalam Time; Bandar Seri Begawan, Brunei
        CCT("CCT","GMT+8:00"),
        CST("CST","GMT+8:00"),
        MYT("MYT","GMT+8:00"), // Malaysia Time; Kuala Lumpur, Malaysia
        PHT("PHT","GMT+8:00"), // Philippine Time; Manila, Philippines
        SGT("SGT","GMT+8:00"),
        WADT("WADT","GMT+8:00"),
        WST("WST","GMT+8:00"),
        JT("JT","GMT+7:30"),
        ALMST("ALMST","GMT+7:00"),
        CXT("CXT","GMT+7:00"),                
        ICT("ICT","GMT+7:00"), // Indochina Time; Bangkok
        WAST("WAST","GMT+7:00"),
        WIT("WIT","GMT+7:00"), // Eastern Indonesian Time; Jarkata, Indonesia
        MMT("MMT","GMT+6:30"),                        
        ALMT("ALMT","GMT+6:00"),
        BDT("BDT","GMT+6:00"), // Bangladesh Time; Dhaka, Bangladesh 
        MAWT("MAWT","GMT+6:00"),
        LKT("LKT","GMT+6:00"), // Lanka Time; Colombo, Sri Lanka
        NPT("NPT","GMT+5:45"), // Nepal Time; Katmandu, Nepal
        IOT("IOT","GMT+5:00"),
        MVT("MVT","GMT+5:00"),
        PKT("PKT","GMT+5:00"), // Pakistan Standard Time; Islambad, Pakistan
        TFT("TFT","GMT+5:00"),                                
        AFT("AFT","GMT+4:30"),
        IRST("IRST","GMT+4:30"), // Iran Standard Time; Tehran, Iran
        MSD("MSD","GMT+4:00"), // Moscow Daylight Time
        MSK("MSK","GMT+4:00"), // Moscow Standard Time
        MUT("MUT","GMT+4:00"),
        RET("RET","GMT+4:00"),
        SCT("SCT","GMT+4:00"),        
        IT("IT","GMT+3:30"),
        IRT("IRT","GMT+3:30"),
        AST("AST","GMT+3:00"), // Arabic Standard Time; ex: Baghdad, Iraq
        BT("BT","GMT+3:00"),
        EAT("EAT","GMT+3:00"),
        EEST("EEST","GMT+3:00"), // Eastern European Summer Time; Sofia, Bulgaria
        EETDST("EETDST","GMT+3:00"),
        HMT("HMT","GMT+3:00"),
        LDT("LDT","GMT+3:00"),
        BDST("BDST","GMT+2:00"),
        CAT("CAT","GMT+2:00"), // Central Africa Time
        CEST("CEST","GMT+2:00"),        
        CETDST("CETDST","GMT+2:00"),
        EET("EET","GMT+2:00"),
        FWT("FWT","GMT+2:00"),
        IST("IST","GMT+2:00"),        
        MEST("MEST","GMT+2:00"),
        METDST("METDST","GMT+2:00"),
        SAST("SAST","GMT+2:00"), // South Africa Standard Time; Johannesburg, South Africa
        SST("SST","GMT+2:00"),        
        BST("BST","GMT+1:00"),
        CET("CET","GMT+1:00"),
        DNT("DNT","GMT+1:00"),        
        FST("FST","GMT+1:00"),
        MET("MET","GMT+1:00"),
        MEWT("MEWT","GMT+1:00"),        
        MEZ("MEZ","GMT+1:00"),
        NOR("NOR","GMT+1:00"),
        SET("SET","GMT+1:00"),        
        SWT("SWT","GMT+1:00"),
        WEST("WEST","GMT+1:00"), // Western European Summer Time; Rabat, Morocco
        WETDST("WETDST","GMT+1:00"),
        GMT("GMT","GMT"),
        UT("UT","GMT"),
        UTC("UTC","GMT"),
        ZULU("ZULU","GMT"),
        WET("WET","GMT"),
        CVT("CVT","GMT-1:00"), // Cape Verde Time; Praia, Cape Verde Island
        FNST("FNST","GMT-1:00"),
        WAT("WAT","GMT-1:00"),
        FNT("FNT","GMT-2:00"),                
        BRST("BRST","GMT-2:00"),                        
        NDT("NDT","GMT-2:30"),
        ADT("ADT","GMT-3:00"),
        ART("ART","GMT-3:00"),
        AWT("AWT","GMT-3:00"),                    
        BRT("BRT","GMT-3:00"),                            
        LST("LST","GMT-3:00"),
        UYT("UYT","GMT-3:00"), // Uruguay Time; Montevideo, Uruguay
        NFT("NFT","GMT-3:30"),
        NST("NST","GMT-3:30"),            
        BOT("BOT","GMT-4:00"), // Bolivia Time; La Paz, Bolivia
        CLT("CLT","GMT-4:00"), // Chile Standard Time; Santiago, Chile
        EDT("EDT","GMT-4:00"),
        PYT("PYT","GMT-4:00"), // Paraguay Time; Asuncion, Paraguay
        ACT("ACT","GMT-5:00"),
        CDT("CDT","GMT-5:00"),
        COT("COT","GMT-5:00"), // Colombia Time; Bogota, Colombia
        ECT("ECT","GMT-5:00"), // Ecuador Time; Quito, Ecuador
        EST("EST","GMT-5:00"),            
        PET("PET","GMT-5:00"), // Peru Time; Lima, Peru
        MDT("MDT","GMT-6:00"),            
        MST("MST","GMT-7:00"),
        PDT("PDT","GMT-7:00"),
        AKDT("AKDT","GMT-8:00"), // Alaska Daylight Time
        PST("PST","GMT-8:00"),
        YDT("YDT","GMT-8:00"),
        AKST("AKST","GMT-9:00"), // Alaska Standard Time
        HDT("HDT","GMT-9:00"),
        YST("YST","GMT-9:00"),            
        MART("MART","GMT-9:30"),                    
        AHST("AHST","GMT-10:00"),
        HST("HST","GMT-10:00"),
        NT("NT","GMT-11:00"),
        IDLW("IDLW","GMT-12:00");

        public String mGlobalTz;
        public String mGmtTz;

        TzTransition(String glbTz, String gmtTz) {
            this.mGlobalTz = glbTz;
            this.mGmtTz = gmtTz;
        }
    }

}
