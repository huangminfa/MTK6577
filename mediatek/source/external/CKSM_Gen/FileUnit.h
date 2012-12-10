// FileUnit.h: interface for the FileUnit class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_FILEUNIT_H__AAED5943_60D1_4164_BD6B_826A5BB7CD27__INCLUDED_)
#define AFX_FILEUNIT_H__AAED5943_60D1_4164_BD6B_826A5BB7CD27__INCLUDED_

/*
#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000
*/
#include <string>
typedef bool (*COMPARE_METHOD)(const std::string &file_name, std::string &pattern);

typedef struct _BIN_FILE
{
	//char* file_name;
    //char* cfg_item_name;
    char file_name[32]; 
    char cfg_item_name[32];	
} BIN_FILE;

class FileUnit
{
public:
	FileUnit ();
	FileUnit (const FileUnit& fu);
	FileUnit& operator= (const FileUnit& fu);
	//void* operator new (unsigned int num);
	//void operator delete (void* p);
	FileUnit (const std::string& path);
	virtual ~FileUnit();
	
	void Open (const std::string& path);
	unsigned int Calc_Checksum(const bool is_check_by_pkg);
	
	void Set_File_Name (std::string fs_name, BIN_FILE bf);
	unsigned int Get_Checksum ();
	unsigned int Get_Size () {return fu_size;}
	std::string Get_Item_Name(){return bin_def.cfg_item_name;}
	bool is_check_by_pkg;

private:
	unsigned int PaddingFilesize2Even(const unsigned int size);
	void PaddingBuf(const unsigned int file_len, unsigned char* buf);
	void MallocfileBuf(const unsigned int buf_size);
	void FreefileBuf(void);
	unsigned int CalcCheckSumByBuf();
	unsigned short CalcCheckSumByPackage();
protected:
	BIN_FILE bin_def;
	std::string fu_name;
	unsigned int fu_checksum;
	unsigned short fu_checksum_by_package;
	unsigned int fu_size;
	unsigned char* fu_buf;
};

#endif // !defined(AFX_FILEUNIT_H__AAED5943_60D1_4164_BD6B_826A5BB7CD27__INCLUDED_)
