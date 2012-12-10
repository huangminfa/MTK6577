#ifndef _FILE_ULTILTY_
#define _FILE_ULTILTY_


#include <vector>
#include "Ini_Unit.h"
#include "FileUnit.h"


class FU_VECTOR
{
public:
	FU_VECTOR ();
	virtual ~FU_VECTOR ();
	std::string GetAppDirectory ();
	void SetAppDirectory (std::string dir);
	std::string GetDestDirectory ();
	void SetDestDirectory (std::string dir);
	std::string GetSrcDirectory ();
	void SetSrcDirectory (std::string dir);
	void SetIniDirectory (std::string dir);
	bool SearchFile( std::string &files,
							    std::string pattern, 
								COMPARE_METHOD compare_m);
	
	FileUnit Append_FileUnit (const FileUnit &fu);
	FileUnit FU_List_Init (const bool is_check_by_pkg);
	int Write_INI ();
	
protected:	
	std::vector<FileUnit> m_fu_vector;
	Ini_Unit m_ini_file;
    Ini_Unit m_cksm_gen_file;
	std::string m_Current_Dir;
	std::string m_Src_Dir;
	std::string m_Dest_Dir;
};

#endif