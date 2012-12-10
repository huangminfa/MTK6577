// Ini_Unite.h: interface for the Ini_Unite class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_INI_UNITE_H__49839613_BD1F_420A_A22F_C05382CBCFF6__INCLUDED_)
#define AFX_INI_UNITE_H__49839613_BD1F_420A_A22F_C05382CBCFF6__INCLUDED_
/*
#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000
*/
#include <string>

class Ini_Unit  
{
public:
	Ini_Unit();
	Ini_Unit(std::string path);
	virtual ~Ini_Unit();

	int Write (const char* lpSection, const char* lpKeyName, const char* lpString);
	int Read (const char* section, const char* lpKeyName, const char* lpDefault,
		        char* lpReturnedString, unsigned short nSize);
    //int WriteProfileString (const char* path, const char* section, 
    //                    const char* lpKeyName, const char* lpString);
    //int ReadProfileString (const char* path, const char* section,
    //                   const char* lpKeyName, const char* lpDefault,
	//	                char* lpReturnedString, unsigned short nSize);

protected:
	std::string ini_path;
    //char *writeBuf;
    //char *readBuf;

    int WriteProfileString (const char* path, const char* section, 
                        const char* lpKeyName, const char* lpString);
    int ReadProfileString (const char* path, const char* section,
                       const char* lpKeyName, const char* lpDefault,
		                char* lpReturnedString, unsigned short nSize);
    int StrCopyWithoutSpace(char *out, const char *src, unsigned int n);
};

#endif // !defined(AFX_INI_UNITE_H__49839613_BD1F_420A_A22F_C05382CBCFF6__INCLUDED_)
