import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {AppConfig} from "../app.config";
import has from 'lodash/has';

const REPORT_URL_HASH = {
  'TOP_5': 'albums/top-5-languages',
  'LAST_10_YEAR': 'albums/per-year-last-10',
  'TOP_10_AREA': 'artists/top-10-area'
};

@Injectable()
export class ReportingService {
  private static SERVICE_URL = 'api/1.0/reporting';
  constructor(
    private http: HttpClient,
    private config: AppConfig
  ) {}

  getReportsURL(reportType: string): string {
    if (!has(REPORT_URL_HASH, reportType)) {
      throw new Error('Unknown report type');
    }
    return `${this.config.apiURL}/${ReportingService.SERVICE_URL}/${REPORT_URL_HASH[reportType]}`;
  }

  getReports(reportType: string): Promise<Array<{key:string, value: string}>> {
    return this.http.get(this.getReportsURL(reportType))
      .map((response: any) => {
        return response as Array<{key:string, value: string}>;
      })
      .toPromise();
  }
}
