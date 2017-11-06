import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {AppConfig} from "../app.config";
import {Language} from "../models/album";

const mapToLanguage = ({
  _id,
  name
}): Language => ({
  code: _id,
  name
});

@Injectable()
export class LanguageService {
  private static SERVICE_URL = '/api/1.0/languages';
  constructor(
    private http: HttpClient,
    private config: AppConfig
  ) {}

  getAllLanguages(): Promise<Array<Language>> {
    return this.http.get(`${this.config.apiURL}${LanguageService.SERVICE_URL}/`)
      .map((response: any) => {
        console.log('Languages: ', response);
        return response.map(mapToLanguage);
      })
      .toPromise();
  }
}
