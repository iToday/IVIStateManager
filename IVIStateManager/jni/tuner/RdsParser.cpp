#include "RdsParser.h"
#include "include/band.h"

#define TAG "RDS"

#include "../include/ilog.h"

using namespace android;

RdsParser::RdsParser(IRdsListener* client){
	listener = client;
}

RdsParser::~RdsParser(){

	if (listener != NULL)
		delete listener;

	listener = NULL;
}

bool RdsParser::parse(unsigned char *info, int len){

	unsigned char RdsInts;
	unsigned char RdsSync;
	unsigned char GrpLost;
	unsigned char RdsFifoUsed;
	unsigned short BlockA;
	unsigned short BlockB;
	unsigned short BlockC;
	unsigned short BlockD;

	unsigned char TP;
	unsigned char PTY;
	unsigned char PI;

	if (len < 16)
		return false;

	RdsInts     = info[1];
	RdsSync     = !!(info[2] & 0x02);
	GrpLost     = !!(info[2] & 0x02);
	TP			= !!(info[3] & 0x20);
	PTY			= info[3] & 0x1F;
	PI          = ((unsigned short)info[4]  << 8) | (unsigned short)info[5];
	RdsFifoUsed = info[6];
	BleA        = (info[7] & 0xC0) >> 6;
	BleB        = (info[7] & 0x30) >> 4;
	BleC        = (info[7] & 0x0C) >> 2;
	BleD        = (info[7] & 0x03) >> 0;
	BlockA      = ((unsigned short)info[8]  << 8) | (unsigned short)info[9];
	BlockB      = ((unsigned short)info[10] << 8) | (unsigned short)info[11];
	BlockC      = ((unsigned short)info[12] << 8) | (unsigned short)info[13];
	BlockD      = ((unsigned short)info[14] << 8) | (unsigned short)info[15];

	if(RdsFifoUsed)
	{
		unsigned char bler[4];
		unsigned char field ;

		// Gather the latest BLER info
		bler[0] = BleA;
		bler[1] = BleB;
		bler[2] = BleC;
		bler[3] = BleD;

		errorCount = 0;
		//RdsGroups++;
		for (field = 0; field <= 3; field++)
		{
			if (bler[field] == UNCORRECTABLE)
			{
				errorCount++;
			}
			else
			{
				//RdsValid[field]++;
			}
		}

		if (errorCount < 4)
		{
			//RdsBlocksValid += (4 - errorCount);
		}

		// Update pi code.
		if (BleA < CORRECTED_THREE_TO_FIVE)
		{
			update_pi(BlockA);
		}

		if (BleB <= CORRECTED_ONE_TO_TWO)
		{
			group_type = BlockB >> 11;  // upper five bits are the group type and version
			// Check for group counter overflow and divide all by 2
			/*if((RdsGroupCounters[group_type] + 1) == 0)
			{
				unsigned char i;
				for (i=0;i<32;i++)
				{
					RdsGroupCounters[i] >>= 1;
				}
			}
			RdsGroupCounters[group_type] += 1;*/
		}
		else
		{
			// Drop the data if more than two errors were uncorrected in block B
			return false;
		}


		// Update pi code.  Version B formats always have the pi code in words A and C
		if (group_type & 0x01)
		{
			update_pi(BlockC);
		}

		// update pty code.
		update_pty((BlockB >> 5) & 0x1f);

		switch (group_type)
		{
			case RDS_TYPE_0A:
				if (BleC <= CORRECTED_THREE_TO_FIVE)
				{
					update_alt_freq(BlockC);
				}
				// fallthrough
			case RDS_TYPE_0B:
				addr = (BlockB & 0x3) * 2;
				if (BleD <= CORRECTED_THREE_TO_FIVE)
				{
					update_ps(addr+0, BlockD >> 8  );
					update_ps(addr+1, BlockD & 0xff);
				}
				break;

			case RDS_TYPE_2A:
			{
				rtblocks[0] = (BlockC >> 8);
				rtblocks[1] = (BlockC & 0xFF);
				rtblocks[2] = (BlockD >> 8);
				rtblocks[3] = (BlockD & 0xFF);
				addr = (BlockB & 0xf) * 4;
				abflag = (BlockB & 0x0010) >> 4;
				update_rt_simple(abflag, 4, addr, rtblocks);
				update_rt_advance(abflag, 4, addr, rtblocks);
				break;
			}

			case RDS_TYPE_2B:
			{
				rtblocks[0] = (BlockD >> 8);
				rtblocks[1] = (BlockD & 0xFF);
				rtblocks[2] = 0;
				rtblocks[3] = 0;
				addr = (BlockB & 0xf) * 2;
				abflag = (BlockB & 0x0010) >> 4;
				// The last 32 bytes are unused in this format
				rtSimple[32]  = 0x0d;
				rtTmp0[32]    = 0x0d;
				rtTmp1[32]    = 0x0d;
				rtCnt[32]     = RT_VALIDATE_LIMIT;
				update_rt_simple (abflag, 2, addr, rtblocks);
				update_rt_advance(abflag, 2, addr, rtblocks);
				break;
			}
			case RDS_TYPE_4A:
				// Clock Time and Date
				update_clock(BlockB, BlockC, BlockD);
				break;
			default:
				break;
		}

		return false;
	}
	return true;
}


//-----------------------------------------------------------------------------
// This routine adds an additional level of error checking on the PI code.
// A PI code is only considered valid when it has been identical for several
// groups.
//-----------------------------------------------------------------------------
void RdsParser::update_pi(unsigned short current_pi)
{
	unsigned char   rds_pi_validate_count  = 0;
	unsigned short  rds_pi_nonvalidated    = 0;

    // if the pi value is the same for a certain number of times, update
    // a validated pi variable
    if (rds_pi_nonvalidated != current_pi)
    {
        rds_pi_nonvalidated =  current_pi;
        rds_pi_validate_count = 1;
    }
    else
    {
        rds_pi_validate_count++;
    }

    if (rds_pi_validate_count > RDS_PI_VALIDATE_LIMIT)
    {
        piDisplay = rds_pi_nonvalidated;

        if (listener != NULL)
             listener->onPi(ptyDisplay);
    }
}

//-----------------------------------------------------------------------------
// This routine adds an additional level of error checking on the PTY code.
// A PTY code is only considered valid when it has been identical for several
// groups.
//-----------------------------------------------------------------------------
void RdsParser::update_pty(unsigned char current_pty)
{
	unsigned char rds_pty_validate_count = 0;
	unsigned char rds_pty_nonvalidated   = 0;

    // if the pty value is the same for a certain number of times, update
    // a validated pty variable
    if (rds_pty_nonvalidated != current_pty)
    {
        rds_pty_nonvalidated =  current_pty;
        rds_pty_validate_count = 1;
    }
    else
    {
        rds_pty_validate_count++;
    }

    if (rds_pty_validate_count > RDS_PTY_VALIDATE_LIMIT)
    {
        ptyDisplay = rds_pty_nonvalidated;

        if (listener != NULL)
        	listener->onPty(ptyDisplay);
    }

}


//-----------------------------------------------------------------------------
// This implelentation of the Program Service update attempts to display
// only complete messages for stations who rotate text through the PS field
// in violation of the RBDS standard as well as providing enhanced error
// detection.
//-----------------------------------------------------------------------------
void RdsParser::update_ps(unsigned char addr, unsigned char byte)
{
    unsigned char i;
    unsigned char textChange = 0; // indicates if the PS text is in transition
    unsigned char psComplete = 1; // indicates that the PS text is ready to be displayed

    if(psTmp0[addr] == byte)
    {
        // The new byte matches the high probability byte
        if(psCnt[addr] < PS_VALIDATE_LIMIT)
        {
            psCnt[addr]++;
        }
        else
        {
            // we have recieved this byte enough to max out our counter
            // and push it into the low probability array as well
            psCnt[addr] = PS_VALIDATE_LIMIT;
            psTmp1[addr] = byte;
        }
    }
    else if(psTmp1[addr] == byte)
    {
        // The new byte is a match with the low probability byte. Swap
        // them, reset the counter and flag the text as in transition.
        // Note that the counter for this character goes higher than
        // the validation limit because it will get knocked down later
        if(psCnt[addr] >= PS_VALIDATE_LIMIT)
        {
            textChange = 1;
            psCnt[addr] = PS_VALIDATE_LIMIT + 1;
        }
        else
        {
            psCnt[addr] = PS_VALIDATE_LIMIT;
        }
        psTmp1[addr] = psTmp0[addr];
        psTmp0[addr] = byte;
    }
    else if(!psCnt[addr])
    {
        // The new byte is replacing an empty byte in the high
        // proability array
        psTmp0[addr] = byte;
        psCnt[addr] = 1;
    }
    else
    {
        // The new byte doesn't match anything, put it in the
        // low probablity array.
        psTmp1[addr] = byte;
    }

    if(textChange)
    {
        // When the text is changing, decrement the count for all
        // characters to prevent displaying part of a message
        // that is in transition.
        for(i=0; i<sizeof(psCnt); i++)
        {
            if(psCnt[i] > 1)
            {
                psCnt[i]--;
            }
        }
    }

    // The PS text is incomplete if any character in the high
    // probability array has been seen fewer times than the
    // validation limit.
    for (i=0;i<sizeof(psCnt);i++)
    {
        if(psCnt[i] < PS_VALIDATE_LIMIT)
        {
            psComplete = 0;
            break;
        }
    }

    // If the PS text in the high probability array is complete
    // copy it to the display array
    if (psComplete)
    {
        for (i=0;i<sizeof(psDisplay); i++)
        {
        	psDisplay[i] = psTmp0[i];
        }

        if (listener != NULL)
        	listener->onPs((char*)psDisplay);
    }
}

//-----------------------------------------------------------------------------
// The basic implementation of the Radio Text update displays data
// immediately but does no additional error detection.
//-----------------------------------------------------------------------------
void RdsParser::update_rt_simple(unsigned char abFlag, unsigned char count, unsigned char addr, unsigned char * chars)
{
    unsigned char errCount;
    unsigned char blerMax;
    unsigned char i,j;

    // If the A/B flag changes, wipe out the rest of the text
    if ((abFlag != rtsFlag) && rtsFlagValid)
    {
        for (i=addr;i<sizeof(rtDislpay);i++)
        {
            rtSimple[i] = 0;
        }
    }
    rtsFlag = abFlag;    // Save the A/B flag
    rtsFlagValid = 1;    // Now the A/B flag is valid

    for (i=0; i<count; i++)
    {
        // Choose the appropriate block. Count > 2 check is necessary for 2B groups
        if ((i < 2) && (count > 2))
        {
            errCount = BleC;
            blerMax = rdsBlerMax[2];
        }
        else
        {
            errCount = BleD;
            blerMax = rdsBlerMax[3];
        }

        if(errCount <= blerMax)
        {
            // Store the data in our temporary array
            rtSimple[addr+i] = chars[i];
            if(chars[i] == 0x0d)
            {
                // The end of message character has been received.
                // Wipe out the rest of the text.
                for (j=addr+i+1;j<sizeof(rtSimple);j++)
                {
                    rtSimple[j] = 0;
                }
                break;
            }
        }
    }

    // Any null character before this should become a space
    for (i=0;i<addr;i++)
    {
        if(!rtSimple[i])
        {
            rtSimple[i] = ' ';
        }
    }
}

//-----------------------------------------------------------------------------
// This implelentation of the Radio Text update attempts to display
// only complete messages even if the A/B flag does not toggle as well
// as provide additional error detection. Note that many radio stations
// do not implement the A/B flag properly. In some cases, it is best left
// ignored.
//-----------------------------------------------------------------------------
void RdsParser::display_rt(void)
{
    unsigned char rtComplete = 1;
    unsigned char i;

    // The Radio Text is incomplete if any character in the high
    // probability array has been seen fewer times than the
    // validation limit.
    for (i=0; i<sizeof(rtTmp0); i++)
    {
        if(rtCnt[i] < RT_VALIDATE_LIMIT)
        {
            rtComplete = 0;
            break;
        }

        if(rtTmp0[i] == 0x0d)
        {
            // The array is shorter than the maximum allowed
            break;
        }
    }

    // If the Radio Text in the high probability array is complete
    // copy it to the display array
    if (rtComplete)
    {
        for (i=0; i<sizeof(rtDislpay); i++)
        {
        	rtDislpay[i] = rtTmp0[i];
            if(rtTmp0[i] == 0x0d)
            {
                break;
            }
        }

        // Wipe out everything after the end-of-message marker
        for (i++; i<sizeof(rtDislpay); i++)
        {
        	rtDislpay[i] = 0;
            //rtCnt[i]     = 0;
            rtTmp0[i]    = 0;
            rtTmp1[i]    = 0;
        }

        for (i=0; i<sizeof(rtCnt); i++)
        {
            rtCnt[i]  = 0;
        }

        if (listener != NULL)
        	listener->onRadioText((char*)rtDislpay);
    }
}

//-----------------------------------------------------------------------------
// This implementation of the Radio Text update attempts to further error
// correct the data by making sure that the data has been identical for
// multiple receptions of each byte.
//-----------------------------------------------------------------------------
void RdsParser::update_rt_advance(unsigned char abFlag, unsigned char count, unsigned char addr, unsigned char * byte)
{
    unsigned char i;
    unsigned char textChange = 0; // indicates if the Radio Text is changing

    if (abFlag != rtFlag && rtFlagValid /*&& !rdsIgnoreAB*/)
    {
        // If the A/B message flag changes, try to force a display
        // by increasing the validation count of each byte
        // and stuffing a space in place of every NUL char
        for (i=0; i<sizeof(rtCnt); i++)
        {
            if (!rtTmp0[i])
            {
                //rtTmp0[i] = ' ';
                rtCnt[i]++;
            }
        }

        for (i=0; i<sizeof(rtCnt); i++)
        {
            rtCnt[i]++;
        }

        display_rt();

        // Wipe out the cached text
        for (i=0; i<sizeof(rtCnt); i++)
        {
            rtCnt[i]  = 0;
            rtTmp0[i] = 0;
            rtTmp1[i] = 0;
        }
    }

    rtFlag = abFlag;    // Save the A/B flag
    rtFlagValid = 1;    // Our copy of the A/B flag is now valid

    for (i=0; i<count; i++)
    {
        unsigned char errCount;
        unsigned char blerMax;
        // Choose the appropriate block. Count > 2 check is necessary for 2B groups
        if ((i < 2) && (count > 2))
        {
            errCount = BleC;
            blerMax = rdsBlerMax[2];
        }
        else
        {
            errCount = BleD;
            blerMax = rdsBlerMax[3];
        }

        if (errCount <= blerMax)
        {
            if(!byte[i])
            {
                //  byte[i] = ' '; // translate nulls to spaces
            }

            // The new byte matches the high probability byte
            if(rtTmp0[addr+i] == byte[i])
            {
                if(rtCnt[addr+i] < RT_VALIDATE_LIMIT)
                {
                    rtCnt[addr+i]++;
                }
                else
                {
                    // we have recieved this byte enough to max out our counter
                    // and push it into the low probability array as well
                    rtCnt[addr+i] = RT_VALIDATE_LIMIT;
                    rtTmp1[addr+i] = byte[i];
                }
            }
            else if(rtTmp1[addr+i] == byte[i])
            {
                // The new byte is a match with the low probability byte. Swap
                // them, reset the counter and flag the text as in transition.
                // Note that the counter for this character goes higher than
                // the validation limit because it will get knocked down later
                if(rtCnt[addr+i] >= RT_VALIDATE_LIMIT)
                {
                    textChange = 1;
                    rtCnt[addr+i] = RT_VALIDATE_LIMIT + 1;
                }
                else
                {
                    rtCnt[addr+i] = RT_VALIDATE_LIMIT;
                }
                rtTmp1[addr+i] = rtTmp0[addr+i];
                rtTmp0[addr+i] = byte[i];
            }
            else if(!rtCnt[addr+i])
            {
                // The new byte is replacing an empty byte in the high
                // proability array
                rtTmp0[addr+i] = byte[i];
                rtCnt[addr+i] = 1;

               // if(!byte[i])
               // {
               //     rtCnt[addr+i]++;
                //}
            }
            else
            {
                // The new byte doesn't match anything, put it in the
                // low probablity array.
                rtTmp1[addr+i] = byte[i];
            }
        }
    }

    if(textChange)
    {
        // When the text is changing, decrement the count for all
        // characters to prevent displaying part of a message
        // that is in transition.
        for(i=0; i<sizeof(rtCnt); i++)
        {
            if(rtCnt[i] > 1)
            {
                rtCnt[i]--;
            }
        }
    }

    // Display the Radio Text
    display_rt();
}

//-----------------------------------------------------------------------------
// Decode the RDS AF data into an array of AF frequencies.
//-----------------------------------------------------------------------------
void RdsParser::update_alt_freq(unsigned short current_alt_freq)
{
    // Currently this only works well for AF method A, though AF method B
    // data will still be captured.
	unsigned char dat;
	unsigned char i;
	unsigned short freq;

    // the top 8 bits is either the AF Count or AF Data
    dat = (unsigned char)(current_alt_freq >> 8);
    // look for the AF Count indicator
    if ((dat >= AF_COUNT_MIN) && (dat <= AF_COUNT_MAX) && ((dat - AF_COUNT_MIN) != afCount))
    {
        //init_alt_freq();  // clear the alternalte frequency list
    	memset(afList, 0, sizeof (afList));

        afCount = (dat - AF_COUNT_MIN); // set the count
        dat = (unsigned char)current_alt_freq;
        if (dat >= AF_FREQ_MIN && dat <= AF_FREQ_MAX)
        {
            freq = AF_FREQ_TO_U16F(dat);
            afList[0]= freq;
        }
    }
    // look for the AF Data
    else if (afCount && dat >= AF_FREQ_MIN && dat <= AF_FREQ_MAX)
    {
    	unsigned char foundSlot = 0;
        static unsigned char clobber=1;  // index to clobber if no empty slot is found
        freq = AF_FREQ_TO_U16F(dat);
        for (i=1; i < afCount; i+=2)
        {
            // look for either an empty slot or a match
            if ((!afList[i]) || (afList[i] == freq))
            {
                afList[i] = freq;
                dat = (unsigned char)current_alt_freq;
                freq = AF_FREQ_TO_U16F(dat);
                afList[i+1] = freq;
                foundSlot = 1;
                break;
            }
        }
        // If no empty slot or match was found, overwrite a 'random' slot.
        if (!foundSlot)
        {
            clobber += (clobber&1) + 1; // this ensures that an odd slot is always chosen.
            clobber %= (afCount);       // keep from overshooting the array
            afList[clobber] = freq;
            dat = (unsigned char)current_alt_freq;
            freq = AF_FREQ_TO_U16F(dat);
            afList[clobber+1] = freq;
        }
    }

}

//-----------------------------------------------------------------------------
// Decode the RDS time data into its individual parts.
//-----------------------------------------------------------------------------
void RdsParser::update_clock(unsigned short b, unsigned short c, unsigned short d)
{

    if (BleB <= rdsBlerMax[1] &&
        BleC <= rdsBlerMax[2] &&
        BleD <= rdsBlerMax[3] &&
        (BleB + BleC + BleD) <= rdsBlerMax[1]) {

        ctDayHigh = (b >> 1) & 1;
        ctDayLow  = (b << 15) | (c >> 1);
        ctHour    = ((c&1) << 4) | (d >> 12);
        ctMinute  = (d>>6) & 0x3F;
        ctOffset  = d & 0x1F;
        if (d & (1<<5))
        {
            ctOffset = -ctOffset;
        }
    }
}
